package site.ftka.survivalcore.services.playerdata.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.logging.objects.ServiceLogger
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.essentials.logging.LoggingEssential
import java.io.BufferedWriter
import java.io.IOException
import java.io.FileWriter
import java.io.File
import java.util.UUID

class PlayerDataEmergencySubservice(private val service: PlayerDataService, private val plugin: MClass) {
    val logger: ServiceLogger = service.logger

    private val baseFolderPath = "${plugin.dataFolder.absolutePath}\\PlayerData"
    private val emergencyDumpFolderPath = "${baseFolderPath}\\EmergencyDump"

    fun uploadAllDumpsToDatabase() {
        val dumps = getAvailableDumps()

        for (dump in dumps)
            service.output_ss.asyncSet(dump).whenCompleteAsync{ result, _ ->
                if (result) deleteEmergencyDump(dump.uuid)
            }
    }

    fun checkDumpExists(uuid: UUID, deleteIt: Boolean): PlayerData? {
        val emergencyDumpFolderFile = File(emergencyDumpFolderPath)
        if (!emergencyDumpFolderFile.exists() || !emergencyDumpFolderFile.isDirectory) return null

        var dumpFile: File? = null
        for (file in emergencyDumpFolderFile.listFiles { file -> file.extension == "json" && file.name.contains(uuid.toString()) }!!)
            dumpFile = file

        var fileText = dumpFile?.readText()
        if (deleteIt) dumpFile?.delete()
        return service.fromJson(fileText)
    }

    fun getAvailableDumps(): List<PlayerData> {
        val availableDumps = mutableListOf<PlayerData>()

        val emergencyDumpFolderFile = File(emergencyDumpFolderPath)
        if (!emergencyDumpFolderFile.exists() || !emergencyDumpFolderFile.isDirectory) return availableDumps.toList()

        // list every json file
        for (file in emergencyDumpFolderFile.listFiles { file -> file.extension == "json" }) {
            val fileText = file.readText()
            val extractedPlayerData = service.fromJson(fileText)
            extractedPlayerData?.let { availableDumps.add(it) }
        }

        return availableDumps
    }

    // For emergency purposes only, like if it's impossible to save in database.
    fun emergencyDump(playerdata: PlayerData) {
        val saveLocationFolder = File(emergencyDumpFolderPath)
        saveLocationFolder.mkdirs()

        logger.log("Dumping ${playerdata.username} (${playerdata.uuid})", LoggingEssential.LogLevel.HIGH)

        // e.g. EmergencyDump\srleg_3988d2e9-60c4-4d81-bed0-a6b6c2d13080
        val playerdataFile = "${saveLocationFolder.absolutePath}\\${playerdata.username}_${playerdata.uuid}.json"

        val bufferedWriter: BufferedWriter
        try {
            bufferedWriter = BufferedWriter(FileWriter(playerdataFile))
            bufferedWriter.write(playerdata.toJson())
            bufferedWriter.close()
        } catch (_: IOException) { }
    }

    fun deleteAllEmergencyDumps() {
        val emergencyDumpFolderFile = File(emergencyDumpFolderPath)
        if (emergencyDumpFolderFile.exists()) emergencyDumpFolderFile.deleteRecursively()
    }

    fun deleteEmergencyDump(uuid: UUID) {
        val emergencyDumpFolderFile = File(emergencyDumpFolderPath)

        for (file in emergencyDumpFolderFile.listFiles { file -> file.extension == "json" })
            if (file.name.contains(uuid.toString())) file.delete()
    }
}
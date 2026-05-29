package site.ftka.survivalcore.services.playerdata.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.initless.logging.LoggingInitless
import java.io.BufferedWriter
import java.io.IOException
import java.io.FileWriter
import java.io.File
import java.util.UUID

internal class PlayerData_EmergencySubservice(private val service: PlayerDataService, private val plugin: MClass) {
    val logger = service.logger.sub("Emergency")

    // fast access vals
    private val essFwk = plugin.essentialsFwk

    private val emergencyDumpFolderPath = "${service.baseFolderPath}/EmergencyDump"

    init {
        pruneDuplicateDumps()
    }

    fun uploadAllDumpsToDatabase(async: Boolean) {
        val dumps = getAvailableDumps()

        if (!essFwk.database.health) {
            logger.log("Tried to upload all dumps to database but database health check failed. Aborted.")
            return
        }

        for (dump in dumps) {
            val futureGet = service.inout_ss.get(dump.uuid, async)

            futureGet?.whenComplete { result, _ ->
                val shouldUpload = if (result == null) true
                else dump.updateTimestamp > result.updateTimestamp

                if (shouldUpload) {
                    val futureSet = service.inout_ss.set(dump, async)
                    futureSet.whenComplete { success, _ ->
                        if (success) {
                            deleteEmergencyDump(dump.uuid)
                            logger.log("Successfully uploaded and cleared emergency dump for ${dump.uuid}", LoggingInitless.LogLevel.LOW)
                        }
                    }
                } else {
                    // Outdated local dump, database is newer. Delete local copy to clean up.
                    deleteEmergencyDump(dump.uuid)
                }
            }
        }
    }

    fun checkDumpExists(uuid: UUID, deleteIt: Boolean): PlayerData? {
        val emergencyDumpFolderFile = File(emergencyDumpFolderPath)
        if (!emergencyDumpFolderFile.exists() || !emergencyDumpFolderFile.isDirectory) return null

        val files = emergencyDumpFolderFile.listFiles { file -> file.extension == "json" && file.name.contains(uuid.toString()) } ?: emptyArray()
        var dumpFile: File? = null
        for (file in files) {
            dumpFile = file
        }

        val fileText = dumpFile?.readText()
        if (deleteIt) dumpFile?.delete()
        return service.fromJson(fileText)
    }

    fun getAvailableDumps(): List<PlayerData> {
        pruneDuplicateDumps()
        val availableDumps = mutableListOf<PlayerData>()

        val emergencyDumpFolderFile = File(emergencyDumpFolderPath)
        if (!emergencyDumpFolderFile.exists() || !emergencyDumpFolderFile.isDirectory) return availableDumps.toList()

        val files = emergencyDumpFolderFile.listFiles { file -> file.extension == "json" } ?: emptyArray()
        for (file in files) {
            val fileText = file.readText()
            try {
                val extractedPlayerData = service.fromJson(fileText)
                extractedPlayerData?.let { availableDumps.add(it) }
            } catch (e: Exception) {
                logger.log("Fatal error occurred in emergency dump deserialization...")
            }
        }

        return availableDumps
    }

    // For emergency purposes only, like if it's impossible to save in database.
    fun emergencyDump(playerdata: PlayerData) {
        val saveLocationFolder = File(emergencyDumpFolderPath)
        saveLocationFolder.mkdirs()

        // 1. Maintain dump limits: no more than ~500 dumps at a time
        val files = saveLocationFolder.listFiles { file -> file.extension == "json" } ?: emptyArray()
        if (files.size >= 500) {
            val sortedFiles = files.sortedBy { it.lastModified() }
            val excessCount = files.size - 499 // Leave room for the new dump
            for (i in 0 until excessCount) {
                logger.log("Pruning oldest emergency dump due to size limit (>= 500 files): ${sortedFiles[i].name}", LoggingInitless.LogLevel.LOW)
                sortedFiles[i].delete()
            }
        }

        logger.log("Dumping ${playerdata.information?.username} (${playerdata.uuid})", LoggingInitless.LogLevel.HIGH)

        val playerdataFile = File(saveLocationFolder, "${playerdata.information?.username}_${playerdata.uuid}.json")

        try {
            playerdataFile.bufferedWriter().use { writer ->
                writer.write(playerdata.toJson())
            }
        } catch (e: IOException) {
            logger.log("Failed to write emergency dump for ${playerdata.uuid}: ${e.message}", LoggingInitless.LogLevel.HIGH)
        }
    }

    /**
     * Scan all dumps, grouping by UUID. If multiple dumps exist for the same player,
     * keep only the newest one based on internal updateTimestamp (or file lastModified) and delete others.
     */
    fun pruneDuplicateDumps() {
        val emergencyDumpFolderFile = File(emergencyDumpFolderPath)
        if (!emergencyDumpFolderFile.exists() || !emergencyDumpFolderFile.isDirectory) return

        val files = emergencyDumpFolderFile.listFiles { file -> file.extension == "json" } ?: return
        val filesByUuid = mutableMapOf<UUID, MutableList<File>>()

        for (file in files) {
            val uuid = extractUuidFromFilename(file.name) ?: continue
            filesByUuid.getOrPut(uuid) { mutableListOf() }.add(file)
        }

        for ((_, fileList) in filesByUuid) {
            if (fileList.size <= 1) continue

            val fileToKeep = fileList.maxByOrNull { file ->
                val pdata = runCatching { service.fromJson(file.readText()) }.getOrNull()
                pdata?.updateTimestamp ?: file.lastModified()
            }

            for (file in fileList) {
                if (file != fileToKeep) {
                    logger.log("Pruning duplicate emergency dump: ${file.name}", LoggingInitless.LogLevel.LOW)
                    file.delete()
                }
            }
        }
    }

    private fun extractUuidFromFilename(name: String): UUID? {
        return try {
            val withoutExtension = name.substringBeforeLast(".")
            val uuidString = withoutExtension.substringAfterLast("_")
            UUID.fromString(uuidString)
        } catch (_: Exception) {
            null
        }
    }

    fun deleteAllEmergencyDumps() {
        val emergencyDumpFolderFile = File(emergencyDumpFolderPath)
        if (emergencyDumpFolderFile.exists()) emergencyDumpFolderFile.deleteRecursively()
    }

    fun deleteEmergencyDump(uuid: UUID) {
        val emergencyDumpFolderFile = File(emergencyDumpFolderPath)
        val files = emergencyDumpFolderFile.listFiles { file -> file.extension == "json" } ?: return

        for (file in files) {
            if (file.name.contains(uuid.toString())) file.delete()
        }
    }
}
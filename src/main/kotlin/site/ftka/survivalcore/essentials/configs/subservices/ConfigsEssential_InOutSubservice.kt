package site.ftka.survivalcore.essentials.configs.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.configs.ConfigsEssential
import site.ftka.survivalcore.initless.logging.LoggingInitless
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

class ConfigsEssential_InOutSubservice(private val essential: ConfigsEssential, private val plugin: MClass) {
    private val logger = essential.logger.sub("InOut")

    private val configsFolderAbsolutePath = "/${plugin.dataFolder.absolutePath}/configs"

    // Will return <FilenameWithoutExtension, JsonContent>
    fun gatherConfigJson(fileName: String, defaultJson: String): String {
        val configsFolderFile = File(configsFolderAbsolutePath)
        if (!configsFolderFile.exists()) configsFolderFile.mkdirs()

        val configFileAbsolutePath = "${configsFolderAbsolutePath}\\${fileName}.json"
        val configFile = File(configFileAbsolutePath)

        if (!configFile.exists()) {
            logger.log("Config file ${configFile.name} did not exist. Creating it.")
            createConfig(fileName, defaultJson)
            return defaultJson
        }

        return try {
            configFile.readText()
        } catch (e: Exception) {
            logger.log("FATAL ERROR. CONFIG FILE LOAD FAILED: ${configFile.name}. SHUTTING DOWN", LoggingInitless.LogLevel.LOW)
            logger.log(e.message.toString(), LoggingInitless.LogLevel.LOW)
            plugin.server.shutdown()
            defaultJson
        }
    }

    fun createConfig(fileName: String, configJsonToWrite: String, overwrite: Boolean = true) {
        val configsFolderFile = File(configsFolderAbsolutePath)
        if (!configsFolderFile.exists()) configsFolderFile.mkdirs()

        val configFile = File("/${configsFolderAbsolutePath}/$fileName.json")
        if (configFile.exists() && !overwrite) return

        configFile.delete() // idk if necessary, just in case

        val bufferedWriter: BufferedWriter
        try {
            bufferedWriter = BufferedWriter(FileWriter(configFile))
            bufferedWriter.write(configJsonToWrite)
            bufferedWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}
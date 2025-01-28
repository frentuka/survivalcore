package site.ftka.survivalcore.essentials.configs.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.configs.ConfigsEssential
import site.ftka.survivalcore.initless.logging.LoggingInitless
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

internal class ConfigsEssential_InOutSubservice(private val essential: ConfigsEssential, private val plugin: MClass) {
    private val logger = essential.logger.sub("InOut")

    private val configsFolderAbsolutePath = "/${plugin.dataFolder.absolutePath}/configs"

    /**
     * Retrieves the JSON content of a configuration file or creates it if it doesn't exist.
     *
     * This function attempts to read the content of a specified configuration file. If the file
     * doesn't exist, it creates the file with the provided default JSON content. In case of any
     * errors during file reading, it logs the error and shuts down the server.
     *
     * @param fileName The name of the configuration file without the .json extension.
     * @param defaultJson The default JSON content to use if the file needs to be created.
     * @return The JSON content of the configuration file as a String, or the defaultJson if the file was just created or if an error occurred.
     */
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

    /**
     * Creates or overwrites a configuration file with the specified JSON content.
     *
     * This function creates a new configuration file or overwrites an existing one with the
     * provided JSON content. If the config folder doesn't exist, it will be created.
     *
     * @param fileName The name of the configuration file to be created or overwritten, without the .json extension.
     * @param configJsonToWrite The JSON content to write to the file.
     * @param overwrite Whether to overwrite the file if it already exists. Defaults to true.
     */
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
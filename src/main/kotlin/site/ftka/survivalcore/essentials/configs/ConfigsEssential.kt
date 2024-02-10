package site.ftka.survivalcore.essentials.configs

import com.google.gson.Gson
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.configs.interfaces.ConfigFile
import site.ftka.survivalcore.essentials.configs.objects.GeneralConfig
import site.ftka.survivalcore.essentials.logging.LoggingEssential.*
import java.io.File

class ConfigsEssential(private val plugin: MClass) {
    val logger = plugin.loggingEssential.getLog("ConfigEssential", Component.text("Config").color(NamedTextColor.GOLD))

    private val configsFolderAbsolutePath = "${plugin.dataFolder.absolutePath}\\configs"

    /*
        File names
     */
    private enum class configFilesEnum(name: String, val defaultConfigFile: ConfigFile) {
        GENERAL("general_config", GeneralConfig())
    }

    /*
        Where to effectively GET CONFIGS
     */
    fun generalCfg(): GeneralConfig { // returns default if no file found
        val enumEntry = configFilesEnum.GENERAL
        return loadedConfigsMap.computeIfAbsent(enumEntry.name) {
            logger.log(Component.text("FATAL ERROR: GLOBAL CONFIG DID NOT EXIST. SHUTTING DOWN.").color(NamedTextColor.RED), LogLevel.LOW)
            plugin.server.shutdown(); enumEntry.defaultConfigFile
        } as GeneralConfig
    }

    private val loadedConfigsMap = mutableMapOf<String, ConfigFile>()

    fun init() {
        loadConfigsIntoMap()
    }

    fun restart() {
        loadedConfigsMap.clear()
        loadConfigsIntoMap()
    }

    private fun loadConfigsIntoMap() {
        val configsFolderFile = File(configsFolderAbsolutePath)
        if (!configsFolderFile.exists()) configsFolderFile.mkdirs()
        if (configsFolderFile.listFiles()?.size == 0) // no config file, create 'em all
            for (enumValue in configFilesEnum.entries)
                createConfig(enumValue.name, enumValue.defaultConfigFile)

        for (enumValue in configFilesEnum.entries) {
            val configFileAbsolutePath = "$configsFolderAbsolutePath\\${enumValue.name}.json"
            val configFile = File(configFileAbsolutePath)

            if (!configFile.exists()) {
                logger.log("Config file ${configFile.name} does not exist. Creating it.")
                createConfig(enumValue.name, enumValue.defaultConfigFile)
                continue
            }

            try { // load config into map
                val configFileObject = fromJson(configFile.readText())
                configFileObject?.let{
                    loadedConfigsMap[enumValue.name] = it
                    logger.log("Successfully loaded config file: ${enumValue.name}")
                }
            } catch (e: Exception) {
                logger.log("FATAL ERROR: CONFIG FILE DID NOT LOAD. Name: ${configFile.name}. SHUTTING DOWN", LogLevel.LOW)
                plugin.server.shutdown()
            }
        }
    }

    private fun createConfig(fileName: String, defaultConfig: ConfigFile, overwrite: Boolean = true) {
        val configsFolderFile = File(configsFolderAbsolutePath)
        if (!configsFolderFile.exists()) configsFolderFile.mkdirs()

        val configFile = File("$configsFolderAbsolutePath\\$fileName.json")
        if (configFile.exists() && !overwrite) return

        configFile.delete() // idk if necessary, just in case

        // create
        if (configFile.createNewFile()) configFile.writeText(defaultConfig.toJson())
    }

    private fun fromJson(json: String?): ConfigFile? = Gson().fromJson(json, ConfigFile::class.java)
}
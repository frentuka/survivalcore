package site.ftka.survivalcore.essentials.configs

import com.google.gson.Gson
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.configs.configurations.ChatConfig
import site.ftka.survivalcore.essentials.configs.configurations.GeneralConfig
import site.ftka.survivalcore.essentials.configs.configurations.PlayerDataConfig
import site.ftka.survivalcore.essentials.configs.subservices.ConfigsEssential_InOutSubservice
import site.ftka.survivalcore.initless.logging.LoggingInitless.*

/**
 * Class responsible for managing essential configurations for the plugin.
 * This includes handling general configurations, player data configurations,
 * and chat configurations. It provides methods to initialize, restart, stop,
 * and manage configuration files effectively.
 *
 * @param plugin The main plugin instance required for initialization.
 *
 * This class should never be public, it's only intended use is internal
 */
internal class ConfigsEssential(private val plugin: MClass) {

    /**
     * Logger instance for logging messages related to ConfigsEssential.
     */
    val logger = plugin.loggingInitless.getLog("ConfigEssential", Component.text("Config").color(NamedTextColor.GOLD))

    /**
     * API for managing configurations.
     */
    val api = ConfigsAPI(this)

    /**
     * Subservice responsible for input and output operations for configurations.
     */
    val inout_ss = ConfigsEssential_InOutSubservice(this, plugin)

    /**
     * Enum representing default configuration files and their default JSON content.
     *
     * @property filename The name of the configuration file.
     * @property defaultJson The default JSON content for the configuration.
     */
    enum class defaultConfigFilesEnum(val filename: String, val defaultJson: String) {
        GENERAL("general_config", GeneralConfig().toJson()),
        PLAYERDATA("playerdata_config", PlayerDataConfig().toJson()),
        CHAT("chat_config", ChatConfig().toJson())
    }

    // Configuration objects stored in memory

    /** The general configuration object. */
    private var generalConfig: GeneralConfig = GeneralConfig()

    /** The player data configuration object. */
    private var playerdataConfig: PlayerDataConfig = PlayerDataConfig()

    /** The chat configuration object. */
    private var chatConfig: ChatConfig = ChatConfig()

    /**
     * Retrieves the general configuration.
     * @return The [GeneralConfig] object.
     */
    fun generalCfg(): GeneralConfig = generalConfig

    /**
     * Retrieves the player data configuration.
     * @return The [PlayerDataConfig] object.
     */
    fun playerdataCfg(): PlayerDataConfig = playerdataConfig

    /**
     * Retrieves the chat configuration.
     * @return The [ChatConfig] object.
     */
    fun chatConfig(): ChatConfig = chatConfig

    /**
     * Initializes the configuration system by loading configuration files.
     */
    fun init() {
        logger.log("Initializing...", LogLevel.LOW)
        loadConfigs()
    }

    /**
     * Restarts the configuration system by reloading configuration files.
     */
    fun restart() {
        logger.log("Restarting...", LogLevel.LOW)
        loadConfigs()
    }

    /**
     * Stops the configuration system.
     */
    fun stop() {
        logger.log("Stopping...", LogLevel.LOW)
    }

    /**
     * Loads the configuration files into memory and updates them if outdated.
     */
    private fun loadConfigs() {
        // Load general configuration
        val generalEnum = defaultConfigFilesEnum.GENERAL
        val generalConfigJson = inout_ss.gatherConfigJson(generalEnum.filename, generalEnum.defaultJson)
        generalConfig = Gson().fromJson(generalConfigJson, GeneralConfig::class.java) as GeneralConfig
        if (generalConfig.version < GeneralConfig().version) {
            generalConfig.version = GeneralConfig().version
            inout_ss.createConfig(generalEnum.filename, generalConfig.toJson(), true)
        }

        // Load player data configuration
        val playerdataEnum = defaultConfigFilesEnum.PLAYERDATA
        val playerdataConfigJson = inout_ss.gatherConfigJson(playerdataEnum.filename, playerdataEnum.defaultJson)
        playerdataConfig = Gson().fromJson(playerdataConfigJson, PlayerDataConfig::class.java) as PlayerDataConfig
        if (playerdataConfig.version < PlayerDataConfig().version) {
            playerdataConfig.version = PlayerDataConfig().version
            inout_ss.createConfig(playerdataEnum.filename, playerdataConfig.toJson())
        }

        // Load chat configuration
        val chatEnum = defaultConfigFilesEnum.CHAT
        val chatConfigJson = inout_ss.gatherConfigJson(chatEnum.filename, chatEnum.defaultJson)
        chatConfig = Gson().fromJson(chatConfigJson, ChatConfig::class.java) as ChatConfig
        if (chatConfig.version < ChatConfig().version) {
            chatConfig.version = ChatConfig().version
            inout_ss.createConfig(chatEnum.filename, chatConfig.toJson())
        }
    }

    /**
     * Converts a JSON string to a String object.
     * @param json The JSON string to convert.
     * @return The converted string, or null if the input is invalid.
     */
    fun fromJson(json: String?): String? = Gson().fromJson(json, String::class.java)
}

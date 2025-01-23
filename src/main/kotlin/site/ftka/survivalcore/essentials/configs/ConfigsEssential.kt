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

class ConfigsEssential(private val plugin: MClass) {
    val logger = plugin.loggingInitless.getLog("ConfigEssential", Component.text("Config").color(NamedTextColor.GOLD))
    val api = ConfigsAPI(this)

    val inout_ss = ConfigsEssential_InOutSubservice(this, plugin)

    /*
        File names
     */

    enum class defaultConfigFilesEnum(val filename: String, val defaultJson: String) {
        GENERAL("general_config", GeneralConfig().toJson()),
        PLAYERDATA("playerdata_config", PlayerDataConfig().toJson()),
        CHAT("chat_config", ChatConfig().toJson())
    }

    // Where configs are stored
    private var generalConfig: GeneralConfig = GeneralConfig()
    private var playerdataConfig: PlayerDataConfig = PlayerDataConfig()
    private var chatConfig: ChatConfig = ChatConfig()

    /*
        Where to effectively get ready-to-use configs
     */
    fun generalCfg(): GeneralConfig = generalConfig
    fun playerdataCfg(): PlayerDataConfig = playerdataConfig
    fun chatConfig(): ChatConfig = chatConfig

    fun init() {
        logger.log("Initializing...", LogLevel.LOW)
        loadConfigs()
    }

    fun restart() {
        logger.log("Restarting...", LogLevel.LOW)
        loadConfigs()
    }

    fun stop() {
        logger.log("Stopping...", LogLevel.LOW)
    }

    private fun loadConfigs() {
        // for some reason IDE says version check is always false but I think it's failing

        // general
        val generalEnum = defaultConfigFilesEnum.GENERAL
        val generalConfigJson = inout_ss.gatherConfigJson(generalEnum.filename, generalEnum.defaultJson)
        generalConfig = Gson().fromJson(generalConfigJson, GeneralConfig::class.java) as GeneralConfig
        // update file if outdated
        if (generalConfig.version < GeneralConfig().version) { generalConfig.version = GeneralConfig().version; inout_ss.createConfig(generalEnum.filename, generalConfig.toJson(), true) }

        // playerdata
        val playerdataEnum = defaultConfigFilesEnum.PLAYERDATA
        val playerdataConfigJson = inout_ss.gatherConfigJson(playerdataEnum.filename, playerdataEnum.defaultJson)
        playerdataConfig = Gson().fromJson(playerdataConfigJson, PlayerDataConfig::class.java) as PlayerDataConfig
        // update file if outdated
        if (playerdataConfig.version < PlayerDataConfig().version) { playerdataConfig.version = PlayerDataConfig().version; inout_ss.createConfig(playerdataEnum.filename, playerdataConfig.toJson()) }

        // chat
        val chatEnum = defaultConfigFilesEnum.CHAT
        val chatConfigJson = inout_ss.gatherConfigJson(chatEnum.filename, chatEnum.defaultJson)
        chatConfig = Gson().fromJson(chatConfigJson, ChatConfig::class.java) as ChatConfig
        // update file if outdated
        if (chatConfig.version < ChatConfig().version) { chatConfig.version = ChatConfig().version; inout_ss.createConfig(chatEnum.filename, chatConfig.toJson()) }
    }

    fun fromJson(json: String?): String? = Gson().fromJson(json, String::class.java)
}
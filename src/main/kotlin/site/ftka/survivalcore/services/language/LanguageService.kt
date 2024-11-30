package site.ftka.survivalcore.services.language

import com.google.gson.Gson
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import site.ftka.survivalcore.services.ServicesFramework
import site.ftka.survivalcore.services.language.listeners.LanguageServiceListener
import site.ftka.survivalcore.services.language.objects.LanguagePack
import site.ftka.survivalcore.services.language.subservices.LanguageService_InputSubservice
import java.util.*

class LanguageService(private val plugin: MClass, private val services: ServicesFramework) {
    val logger = plugin.loggingInitless.getLog("Language", Component.text("Lang").color(NamedTextColor.WHITE))
    val api = LanguageAPI(this)

    /*
        LanguageService
        Will gather and store detected language packs
        to allow customizable messages to user's choice.
     */

    private var langListener = LanguageServiceListener(this, plugin)

    val input_ss = LanguageService_InputSubservice(this, plugin)

    val defaultLanguagePack = LanguagePack()

    val langMap = mutableMapOf<String, LanguagePack>()
    val playerLangMap = mutableMapOf<UUID, String>()

    fun init() {
        logger.log("Initializing...")

        loadLanguagePacksIntoMap()

        // initialize listeners
        plugin.propEventsInitless.registerListener(langListener)

        for (playerdata in services.playerData.getPlayerDataMap().values)
            playerdata.settings?.let { playerLangMap[playerdata.uuid] = it.language }
    }

    fun restart() {
        logger.log("Restarting...", LogLevel.LOW)
        loadLanguagePacksIntoMap()
    }

    fun loadLanguagePacksIntoMap(clearMap: Boolean = true) {
        if (clearMap) langMap.clear()
        input_ss.gatherAllLanguagePacks().forEach{ langMap[it.name] = it }
    }

    fun fromJson(json: String): LanguagePack {
        val gson = Gson()
        return gson.fromJson(json, LanguagePack::class.java)
    }

}
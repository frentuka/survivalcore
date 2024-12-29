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
    val data = LanguageServiceData(this, plugin)

    /*
        LanguageService
        Will gather and store detected language packs
        to allow customizable messages to user's choice.
     */

    private var langListener = LanguageServiceListener(this, plugin)

    val input_ss = LanguageService_InputSubservice(this, plugin)

    val defaultLanguagePack = LanguagePack()

    fun init() {
        logger.log("Initializing...")

        data.loadLanguagePacksIntoMap()

        // initialize listeners
        plugin.propEventsInitless.registerListener(langListener)
    }

    fun restart() {
        logger.log("Restarting...", LogLevel.LOW)
        data.loadLanguagePacksIntoMap(true)
        data.loadPlayersLanguage(true)
    }

    fun stop() {
        logger.log("Stopping...", LogLevel.LOW)
    }

    fun fromJson(json: String): LanguagePack {
        val gson = Gson()
        return gson.fromJson(json, LanguagePack::class.java)
    }

}
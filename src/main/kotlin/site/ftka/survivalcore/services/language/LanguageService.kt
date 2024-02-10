package site.ftka.survivalcore.services.language

import com.google.gson.Gson
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.ServicesFramework
import site.ftka.survivalcore.services.language.listeners.LanguageServiceListener
import site.ftka.survivalcore.services.language.objects.LanguagePack
import site.ftka.survivalcore.services.language.subservices.LanguageService_InputSubservice
import java.util.*

class LanguageService(private val plugin: MClass, private val services: ServicesFramework) {

    /*
        LanguageService
        Will gather and store detected language packs
        to allow customizable messages to user's choice.
     */

    private var langListener = LanguageServiceListener(this, plugin)

    val input_ss = LanguageService_InputSubservice(this, plugin)

    val langMap = mutableMapOf<String, LanguagePack>()
    val playerLangMap = mutableMapOf<UUID, String>()

    fun init() {
        mapLanguagePacks()

        // initialize listeners
        plugin.propEventsInitless.registerListener(langListener)

        for (playerdata in services.playerDataService.playerDataMap.values)
            playerLangMap[playerdata.uuid] = playerdata.lang.language
    }

    fun restart() {
        mapLanguagePacks()
    }

    fun mapLanguagePacks(clearMap: Boolean = true) {
        if (clearMap) langMap.clear()
        input_ss.gatherAllLanguagePacks().forEach{ langMap[it.name] = it }
    }

    fun fromJson(json: String): LanguagePack {
        val gson = Gson()
        return gson.fromJson(json, LanguagePack::class.java)
    }

}
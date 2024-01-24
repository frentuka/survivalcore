package site.ftka.survivalcore.services.language

import com.google.gson.Gson
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.ServicesCore
import site.ftka.survivalcore.services.language.listeners.LanguageServiceListener
import site.ftka.survivalcore.services.language.objects.LanguagePack
import site.ftka.survivalcore.utils.jsonFileUtils
import java.util.*

class LanguageService(private val plugin: MClass, private val services: ServicesCore) {

    /*
        LanguageService
        Will gather and store detected language packs
        to allow customizable messages to user's choice.
     */

    val userLangMap = mutableMapOf<UUID, String>()

    private val langFolderLocation = "${plugin.dataFolder.absolutePath}\\language"

    private var langListener = LanguageServiceListener(this, plugin)

    private val langMap = mutableMapOf<String, LanguagePack>()

    fun init() {
        initLangMap()

        // initialize listeners
        plugin.eventsEssential.registerListener(langListener)

        for (playerdata in services.playerDataService.playerDataMap.values)
            userLangMap[playerdata.uuid] = playerdata.lang.language
    }

    fun restart() {
        langMap.clear()
        initLangMap()
    }

    private fun initLangMap() {
        readLangPacks().forEach{ langMap[it.name] = it }
    }

    private fun readLangPacks(): List<LanguagePack> {
        val langpacks = mutableListOf<LanguagePack>()
        jsonFileUtils.readAllJson(langFolderLocation).forEach{
            langpacks.add(fromJson(it))
        }

        return langpacks
    }

    private fun fromJson(json: String): LanguagePack {
        val gson = Gson()
        return gson.fromJson(json, LanguagePack::class.java)
    }

}
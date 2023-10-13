package site.ftka.survivalcore.services.language

import com.google.gson.Gson
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.language.listeners.LanguageServiceListener
import site.ftka.survivalcore.services.language.objects.LanguagePack
import site.ftka.survivalcore.utils.jsonFileUtils

class LanguageService(private val plugin: MClass) {

    /*
        LanguageService
        Will gather and store detected language packs
        to allow customizable messages to user's choice.
     */

    private val langFolderLocation = "${plugin.dataFolder.absolutePath}\\language"

    private var langListener = LanguageServiceListener(plugin)

    private val langMap = mutableMapOf<String, LanguagePack>()

    fun init() {
        initLangMap()

        // initialize listeners
        plugin.server.pluginManager.registerEvents(langListener, plugin)
    }

    fun restart() {
        langMap.clear()
        initLangMap()
    }

    private fun initLangMap() {
        readGroupsFromStorage().forEach{ langMap[it.name] = it }
    }

    private fun readGroupsFromStorage(): List<LanguagePack> {
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
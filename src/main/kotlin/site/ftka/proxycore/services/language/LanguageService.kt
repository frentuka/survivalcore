package site.ftka.proxycore.services.language

import com.google.gson.Gson
import site.ftka.proxycore.MClass
import site.ftka.proxycore.services.language.listeners.LanguageServiceListener
import site.ftka.proxycore.services.language.objects.LanguagePack
import site.ftka.proxycore.utils.jsonFileUtils

class LanguageService(private val plugin: MClass) {

    /*
        LanguageService
        Will gather and store detected language packs
        to allow customizable messages to user's choice.
     */

    private val langFolderLocation = "${plugin.dataDirectory.toAbsolutePath()}\\language"

    private var langListener = LanguageServiceListener(plugin)

    private val langMap = mutableMapOf<String, LanguagePack>()

    init {
        reloadLang()

        // initialize listeners
        plugin.onProxyInitRunnables.add{
            plugin.server.eventManager.register(langListener, plugin)
        }
    }

    /*
        getters
     */




    /*
        setters
     */



    private fun reloadLang() {
        langMap.clear()
        readGroupsFromStorage().forEach{
            langMap[it.name] = it
        }
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
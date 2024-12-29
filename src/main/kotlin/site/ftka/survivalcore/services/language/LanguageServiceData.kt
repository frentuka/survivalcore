package site.ftka.survivalcore.services.language

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.language.objects.LanguagePack
import java.util.*

class LanguageServiceData(private val service: LanguageService, private val plugin: MClass) {

    val langMap = mutableMapOf<String, LanguagePack>()
    val playerLangMap = mutableMapOf<UUID, String>()

    fun loadLanguagePacksIntoMap(clearMap: Boolean = true) {
        if (clearMap) langMap.clear()
        service.input_ss.gatherAllLanguagePacks().forEach{ langMap[it.name] = it }
    }

    fun loadPlayersLanguage(clearMap: Boolean = true) {
        if (clearMap) playerLangMap.clear()

        plugin.servicesFwk.playerData.data.getPlayerDataSet().forEach {
            it.settings?.let { settings -> playerLangMap[it.uuid] = settings.language }
        }
    }


}
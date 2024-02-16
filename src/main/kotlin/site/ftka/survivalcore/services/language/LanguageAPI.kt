package site.ftka.survivalcore.services.language

import site.ftka.survivalcore.services.language.objects.LanguagePack
import java.util.UUID

class LanguageAPI(private val svc: LanguageService) {

    fun playerLanguagePack(uuid: UUID): LanguagePack {
        val playerLanguagePackName = svc.playerLangMap[uuid]
        return svc.langMap[playerLanguagePackName] ?: svc.defaultLanguagePack
    }

}
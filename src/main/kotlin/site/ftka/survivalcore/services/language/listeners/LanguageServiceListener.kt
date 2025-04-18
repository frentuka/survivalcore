package site.ftka.survivalcore.services.language.listeners

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.proprietaryEvents.annotations.PropEventHandler
import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropListener
import site.ftka.survivalcore.services.language.LanguageService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRegisterEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataUnregisterEvent

class LanguageServiceListener(private val service: LanguageService, private val plugin: MClass): PropListener {

    @PropEventHandler
    fun playerRegisterEvent(e: PlayerDataRegisterEvent) {
        if (e.playerdata == null) return
        e.playerdata.settings?.let { service.data.playerLangMap[e.uuid] = it.language }
    }

    @PropEventHandler
    fun playerUnregisterEvent(e: PlayerDataUnregisterEvent) {
        service.data.playerLangMap.remove(e.uuid)
    }
}
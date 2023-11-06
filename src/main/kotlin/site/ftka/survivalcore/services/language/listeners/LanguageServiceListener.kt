package site.ftka.survivalcore.services.language.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.language.LanguageService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRegisterEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataUnregisterEvent

class LanguageServiceListener(private val service: LanguageService, private val plugin: MClass): Listener {

    @EventHandler
    fun playerRegisterEvent(e: PlayerDataRegisterEvent) {
        if (e.playerdata == null) return
        service.userLangMap[e.uuid] = e.playerdata.lang.language
    }

    @EventHandler
    fun playerUnregisterEvent(e: PlayerDataUnregisterEvent) {
        service.userLangMap.remove(e.uuid)
    }
}
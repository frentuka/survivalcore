package site.ftka.proxycore.services.language.listeners

import org.bukkit.event.EventHandler
import site.ftka.proxycore.MClass
import site.ftka.proxycore.services.playerdata.events.PlayerDataRegisterEvent
import site.ftka.proxycore.services.playerdata.events.PlayerDataUnregisterEvent
import java.util.*

class LanguageServiceListener(plugin: MClass) {
    val services = plugin.services()
    val userLangMap = mutableMapOf<UUID, String>()

    init {
        services.playerDataService.getCachedData().forEach{
            userLangMap[it.key] = it.value.lang
        }
    }

    @EventHandler
    fun playerRegisterEvent(e: PlayerDataRegisterEvent) {
        if (e.playerdata == null) return
        userLangMap[e.uuid] = e.playerdata.lang
    }

    @EventHandler
    fun playerUnregisterEvent(e: PlayerDataUnregisterEvent) {
        userLangMap.remove(e.uuid)
    }
}
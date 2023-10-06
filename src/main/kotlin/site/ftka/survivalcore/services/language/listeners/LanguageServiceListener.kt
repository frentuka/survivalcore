package site.ftka.survivalcore.services.language.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRegisterEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataUnregisterEvent
import java.util.*

class LanguageServiceListener(plugin: MClass): Listener {
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
package site.ftka.proxycore.services.language.listeners

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
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

    @Subscribe(order = PostOrder.LATE)
    fun playerRegisterEvent(e: PlayerDataRegisterEvent) {
        if (e.playerdata == null) return
        userLangMap[e.uuid] = e.playerdata.lang
    }

    @Subscribe(order = PostOrder.LATE)
    fun playerUnregisterEvent(e: PlayerDataUnregisterEvent) {
        userLangMap.remove(e.uuid)
    }
}
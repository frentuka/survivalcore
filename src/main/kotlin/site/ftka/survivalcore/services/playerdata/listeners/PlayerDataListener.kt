package site.ftka.survivalcore.services.playerdata.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataJoinEvent

class PlayerDataListener(private val service: PlayerDataService, private val plugin: MClass): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerLoginEvent) {
        val player_uuid = event.player.uniqueId
        service.onlinePlayers[player_uuid] = event.player.name

        // Is the player cached?
        if (service.playerDataMap.containsKey(player_uuid)) {
            val playerdataJoinEvent = PlayerDataJoinEvent(event.player.uniqueId, service.playerDataMap[event.player.uniqueId])
            plugin.server.pluginManager.callEvent(playerdataJoinEvent)
            return
        }

        // PlayerDataJoinEvent will be fired in finishRegistration after firing PlayerDataRegisterEvent
        service.registration_ss.register(event.player.uniqueId)
    }

    // cache's TTL
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player_uuid = event.player.uniqueId

        // Is the player cached? It should be...
        service.onlinePlayers.remove(player_uuid)

        // Cache entries limit
        service.registration_ss.unregister(player_uuid)
    }
}
package site.ftka.survivalcore.services.playerdata.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.events.PlayerDataJoinEvent
import java.util.UUID

class PlayerDataListener(val plugin: MClass): Listener {
    val services = plugin.services()

    // Map of <UUID, Disconnection system time>
    private var disconnections: MutableMap<UUID, Long> = mutableMapOf()

    // Disconnections timings settings
    private val timeout: Long = 1800*1000 // will unregister player after 1/2 hour
    private val disconnectionsLimit = 25

    @EventHandler
    fun onPlayerJoin(event: PlayerLoginEvent) {
        val player_uuid = event.player.uniqueId
        services.playerDataService.onlinePlayers[player_uuid] = event.player.name

        // Is the player cached?
        disconnections.remove(player_uuid)
        if (services.playerDataService.isCached(player_uuid)) {
            val playerdataJoinEvent = PlayerDataJoinEvent(event.player.uniqueId, services.playerDataService.getCachedPlayer(event.player.uniqueId))
            plugin.server.pluginManager.callEvent(playerdataJoinEvent)
            return
        }

        // PlayerDataJoinEvent will be fired in finishRegistration after firing PlayerDataRegisterEvent
        services.playerDataService.register(event.player.uniqueId)
    }

    // cache's TTL
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player_uuid = event.player.uniqueId
        val currentTime: Long = System.currentTimeMillis()

        // Is the player cached? It should be...
        services.playerDataService.onlinePlayers.remove(player_uuid)
        if (!services.playerDataService.isCached(player_uuid)) {
            disconnections.remove(player_uuid)
            return
        }

        // Add player's disconnection entry
        disconnections[player_uuid] = currentTime

        // Cache entries limit
        while (disconnections.size >= disconnectionsLimit) {
            val lowestEntry = disconnections.minBy { it.value }

            services.playerDataService.unregister(lowestEntry.key)
            disconnections.remove(lowestEntry.key)
        }

        // Check other entries
        disconnections.forEach{
            if (it.value + timeout < currentTime) {
                disconnections.remove(it.key)
                services.playerDataService.unregister(it.key)
            }
        }
    }
}
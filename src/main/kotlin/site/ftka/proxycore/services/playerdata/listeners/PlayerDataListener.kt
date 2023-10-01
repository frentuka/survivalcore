package site.ftka.proxycore.services.playerdata.listeners

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import site.ftka.proxycore.MClass
import site.ftka.proxycore.services.playerdata.events.PlayerDataJoinEvent
import java.util.UUID

class PlayerDataListener(val plugin: MClass) {
    val services = plugin.services()

    // Map of <UUID, Disconnection system time>
    private var disconnections: MutableMap<UUID, Long> = mutableMapOf()

    // Disconnections timings settings
    private val timeout: Long = 1800*1000 // will unregister player after 1/2 hour
    private val disconnectionsLimit = 25

    @Subscribe(order = PostOrder.EARLY)
    fun onPlayerJoin(event: PlayerChooseInitialServerEvent) {
        val player_uuid = event.player.uniqueId
        services.playerDataService.onlinePlayers[player_uuid] = event.player.username

        // Is the player cached?
        disconnections.remove(player_uuid)
        if (services.playerDataService.isCached(player_uuid)) {
            plugin.server.eventManager.fire(PlayerDataJoinEvent(event.player.uniqueId, services.playerDataService.getCachedPlayer(event.player.uniqueId)))
            return
        }

        // PlayerDataJoinEvent will be fired in finishRegistration after firing PlayerDataRegisterEvent
        services.playerDataService.register(event.player.uniqueId)
    }

    // cache's TTL
    @Subscribe(order = PostOrder.EARLY)
    fun onPlayerQuit(event: DisconnectEvent) {
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
package site.ftka.survivalcore.services.playerdata.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.database.events.DatabaseHealthCheckFailedEvent
import site.ftka.survivalcore.services.database.events.DatabaseReconnectEvent
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataJoinEvent
import site.ftka.survivalcore.utils.textUtils

class PlayerDataListener(private val service: PlayerDataService, private val plugin: MClass): Listener {
    private val services = plugin.services

    @EventHandler
    fun onPlayerJoin(event: PlayerLoginEvent) {
        val player_uuid = event.player.uniqueId

        // If database health is false, prevent player from joining
        if (!services.dbService.health) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                textUtils.col("PlayerData was unable to communicate with database"))
            return
        }

        // Add to online players
        service.onlinePlayers[player_uuid] = event.player.name

        // Register
        service.registration_ss.register(event.player.uniqueId)

        // Call event
        val playerdataJoinEvent = PlayerDataJoinEvent(event.player.uniqueId, service.playerDataMap[event.player.uniqueId])
        plugin.server.pluginManager.callEvent(playerdataJoinEvent)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player_uuid = event.player.uniqueId

        // Remove from online players
        service.onlinePlayers.remove(player_uuid)

        // Unregister
        service.registration_ss.unregister(player_uuid)
    }

    // Make emergency dump for every player
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onDatabaseHealthCheckFail(event: DatabaseHealthCheckFailedEvent) {
        service.logger.log("Database health check failed. Dumping all playerdata into storage.")

        for (playerdata in service.playerDataMap.values)
            service.emergency_ss.emergencyDump(playerdata)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onDatabaseReconnect(event: DatabaseReconnectEvent) {
        service.logger.log("Database reconnected. Saving all emergency dumps if available.")

        for (playerdatadump in service.emergency_ss.getAvailableDumps()) {
            service.output_ss.asyncSet(playerdatadump.uuid, playerdatadump).whenComplete{result, _ ->
                if (result) service.emergency_ss.deleteEmergencyDump(playerdatadump.uuid)
            }
        }
    }
}
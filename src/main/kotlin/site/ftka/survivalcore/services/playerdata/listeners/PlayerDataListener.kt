package site.ftka.survivalcore.services.playerdata.listeners

import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.database.events.DatabaseDisconnectEvent
import site.ftka.survivalcore.essentials.database.events.DatabaseReconnectEvent
import site.ftka.survivalcore.essentials.logging.LoggingEssential
import site.ftka.survivalcore.essentials.proprietaryEvents.annotations.PropEventHandler
import site.ftka.survivalcore.essentials.proprietaryEvents.enums.PropEventPriority
import site.ftka.survivalcore.essentials.proprietaryEvents.interfaces.PropListener
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataJoinEvent

class PlayerDataListener(private val service: PlayerDataService, private val plugin: MClass): Listener, PropListener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player_uuid = event.player.uniqueId

        // If database health is false, prevent player from joining
        if (!plugin.dbEssential.health) {
            event.player.kick(Component.text("PlayerData was unable to communicate with database"), PlayerKickEvent.Cause.UNKNOWN)
            return
        }

        // Add to online players
        service.onlinePlayers[player_uuid] = event.player.name

        // Register
        service.registration_ss.register(event.player.uniqueId, event.player)

        // Call event
        val playerdataJoinEvent = PlayerDataJoinEvent(event.player.uniqueId, service.playerDataMap[event.player.uniqueId])
        plugin.eventsEssential.fireEvent(playerdataJoinEvent)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        service.logger.log("Player quit event", LoggingEssential.LogLevel.DEBUG)

        // Remove from online players
        service.onlinePlayers.remove(player.uniqueId)

        service.logger.log("Removed ${event.player.name} from online players", LoggingEssential.LogLevel.DEBUG)

        // Unregister
        service.registration_ss.unregister(player.uniqueId, player)
    }

    // Make emergency dump for every player
    @PropEventHandler(priority = PropEventPriority.FIRST)
    fun onDatabaseHealthCheckFail(event: DatabaseDisconnectEvent) {
        service.logger.log("Database health check failed. Dumping all playerdata into storage.", LoggingEssential.LogLevel.LOW)

        for (playerdata in service.playerDataMap.values) {
            service.emergency_ss.emergencyDump(playerdata)
            plugin.server.getPlayer(playerdata.uuid)?.kick(Component.text("Database connection failed unexpectedly."))
        }
    }

    @PropEventHandler(priority = PropEventPriority.FIRST)
    fun onDatabaseReconnect(event: DatabaseReconnectEvent) {
        service.logger.log("Database reconnected. Saving all emergency dumps if available.", LoggingEssential.LogLevel.LOW)
        service.emergency_ss.uploadAllDumpsToDatabase(false)
    }

    /*
        TESTING
     */
    @EventHandler
    fun onMove(event: EntityDamageEvent) {

    }


}
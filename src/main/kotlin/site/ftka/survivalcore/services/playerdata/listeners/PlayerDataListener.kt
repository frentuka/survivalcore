package site.ftka.survivalcore.services.playerdata.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.database.events.DatabaseDisconnectEvent
import site.ftka.survivalcore.essentials.database.events.DatabaseReconnectEvent
import site.ftka.survivalcore.initless.logging.LoggingInitless
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import site.ftka.survivalcore.initless.proprietaryEvents.annotations.PropEventHandler
import site.ftka.survivalcore.initless.proprietaryEvents.enums.PropEventPriority
import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropListener
import site.ftka.survivalcore.services.playerdata.PlayerDataService

class PlayerDataListener(private val service: PlayerDataService, private val plugin: MClass): Listener, PropListener {

    // fast access vals
    private val essFwk = plugin.essentialsFwk

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player_uuid = event.player.uniqueId

        // disable join message
        event.joinMessage(Component.text(""))

        // If database health is false, prevent player from joining
        if (!essFwk.database.health) {
            event.player.kick(Component.text("Unable to communicate with database"), PlayerKickEvent.Cause.UNKNOWN)
            return
        }

        // Add to online players
        service.data.putOnlinePlayer(player_uuid, event.player.name)

        // Register
        service.registration_ss.register(event.player.uniqueId, event.player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        // disable quit message
        event.quitMessage(Component.text(""))

        service.logger.log("Player quit event", LogLevel.DEBUG)

        // Remove from online players
        service.data.removeOnlinePlayer(player.uniqueId)

        service.logger.log("Removed ${event.player.name} from online players", LoggingInitless.LogLevel.DEBUG)

        // Unregister
        service.registration_ss.unregister(player.uniqueId, player)
    }

    // Make emergency dump for every player
    @PropEventHandler(priority = PropEventPriority.FIRST)
    fun onDatabaseHealthCheckFail(event: DatabaseDisconnectEvent) {
        service.logger.log("Database health check failed. Dumping all playerdata into storage.", LogLevel.LOW, NamedTextColor.RED)

        for (playerdata in service.data.getPlayerDataMap().values) {
            service.emergency_ss.emergencyDump(playerdata)
            plugin.server.getPlayer(playerdata.uuid)?.kick(Component.text("Database connection failed unexpectedly."))
        }
    }

    @PropEventHandler(priority = PropEventPriority.FIRST)
    fun onDatabaseReconnect(event: DatabaseReconnectEvent) {
        service.logger.log("Database reconnected. Saving available emergency dumps.", LoggingInitless.LogLevel.LOW)
        service.emergency_ss.uploadAllDumpsToDatabase(false)
    }
}
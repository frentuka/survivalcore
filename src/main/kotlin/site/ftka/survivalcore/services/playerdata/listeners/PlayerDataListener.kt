package site.ftka.survivalcore.services.playerdata.listeners

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import com.google.gson.Gson
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.database.events.DatabaseHealthCheckFailedEvent
import site.ftka.survivalcore.essentials.database.events.DatabaseReconnectEvent
import site.ftka.survivalcore.essentials.logging.LoggingEssential
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataJoinEvent
import site.ftka.survivalcore.utils.textUtils
import java.util.Base64

class PlayerDataListener(private val service: PlayerDataService, private val plugin: MClass): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerLoginEvent) {
        val player_uuid = event.player.uniqueId

        // If database health is false, prevent player from joining
        if (!plugin.dbEssential.health) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                textUtils.col("PlayerData was unable to communicate with database"))
            return
        }

        // Add to online players
        service.onlinePlayers[player_uuid] = event.player.name

        // Register
        service.registration_ss.register(event.player.uniqueId, event.player.name)

        // Call event
        val playerdataJoinEvent = PlayerDataJoinEvent(event.player.uniqueId, service.playerDataMap[event.player.uniqueId])
        plugin.server.pluginManager.callEvent(playerdataJoinEvent)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player_uuid = event.player.uniqueId

        service.logger.log("Player quit event", LoggingEssential.LogLevel.DEBUG)

        // Remove from online players
        service.onlinePlayers.remove(player_uuid)

        service.logger.log("Removed ${event.player.name} from online players", LoggingEssential.LogLevel.DEBUG)

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
        service.emergency_ss.uploadAllDumpsToDatabase()
    }
}
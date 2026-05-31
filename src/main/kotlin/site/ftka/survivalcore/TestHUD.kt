package site.ftka.survivalcore

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.meta.Damageable
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * A highly isolated and easily removable test system that implements:
 * 1. An action bar showing the player's current block coordinates.
 * 2. A bossbar showing the current held item's name and its durability progression.
 *
 * To disable or remove:
 * - Remove or comment out the initialization in site.ftka.survivalcore.MClass
 * - Delete this file
 */
class TestHUD(private val plugin: MClass) : Listener {

    private val activeBossBars = ConcurrentHashMap<UUID, BossBar>()

    /**
     * Start tracking all online players (useful if reloaded or enabled mid-session).
     */
    fun start() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        for (player in plugin.server.onlinePlayers) {
            startTracking(player)
        }
    }

    /**
     * Stop tracking all players and clean up resources/bossbars.
     */
    fun stop() {
        for (player in plugin.server.onlinePlayers) {
            stopTracking(player)
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        startTracking(event.player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        stopTracking(event.player)
    }

    private fun startTracking(player: Player) {
        val uuid = player.uniqueId
        
        // Ensure we clean up any old bossbar first
        stopTracking(player)

        // Create a new BossBar for the player
        val bossBar = BossBar.bossBar(
            Component.text("Loading...").color(NamedTextColor.GRAY),
            1.0f,
            BossBar.Color.BLUE,
            BossBar.Overlay.PROGRESS
        )
        activeBossBars[uuid] = bossBar
        player.showBossBar(bossBar)

        // Schedule a repeating thread-safe Folia task on the player's EntityScheduler
        // This task will be automatically cancelled by Folia when the player entity is retired (quits)
        player.scheduler.runAtFixedRate(plugin, { task ->
            if (!player.isOnline) {
                task.cancel()
                return@runAtFixedRate
            }
            updateHUD(player, bossBar)
        }, {
            // Task cancellation callback
        }, 1L, 2L) // Runs every 2 ticks (100ms) for extremely smooth updates
    }

    private fun stopTracking(player: Player) {
        val bossBar = activeBossBars.remove(player.uniqueId)
        if (bossBar != null) {
            player.hideBossBar(bossBar)
        }
    }

    private fun updateHUD(player: Player, bossBar: BossBar) {
        // 1. (Removed ActionBar coords to free up ActionBar for notifications)

        // 2. Update BossBar (Held Item & Durability)
        val item = player.inventory.itemInMainHand
        if (item.type.isAir) {
            bossBar.name(Component.text("Empty Hand").color(NamedTextColor.GRAY))
            bossBar.progress(0.0f)
            bossBar.color(BossBar.Color.WHITE)
        } else {
            // Get item display name or fallback to its localized translatable key
            val itemMeta = item.itemMeta
            val itemDisplayName = if (itemMeta?.hasDisplayName() == true) {
                itemMeta.displayName() ?: Component.translatable(item.translationKey())
            } else {
                Component.translatable(item.translationKey())
            }

            // Progression: current durability / max durability
            val maxDurability = item.type.maxDurability.toFloat()
            if (maxDurability > 0 && itemMeta is Damageable) {
                val damage = itemMeta.damage.toFloat()
                val remaining = maxDurability - damage
                val progress = (remaining / maxDurability).coerceIn(0.0f, 1.0f)
                
                bossBar.progress(progress)
                bossBar.name(itemDisplayName)

                // Change bossbar color based on how low the durability is
                when {
                    progress > 0.5f -> bossBar.color(BossBar.Color.GREEN)
                    progress > 0.25f -> bossBar.color(BossBar.Color.YELLOW)
                    else -> bossBar.color(BossBar.Color.RED)
                }
            } else {
                // Non-damageable items show full progress bar
                bossBar.progress(1.0f)
                bossBar.name(itemDisplayName)
                bossBar.color(BossBar.Color.BLUE)
            }
        }
    }
}

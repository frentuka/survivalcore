package site.ftka.survivalcore.apps.WorldBoardTest.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.WorldBoardTest.WorldBoardTestApp

class WorldBoardTestApp_TestCommand(private val plugin: MClass, private val app: WorldBoardTestApp) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Component.text("This command can only be run by a player.").color(NamedTextColor.RED))
            return true
        }

        if (!sender.hasPermission("staff.admin")) {
            sender.sendMessage(Component.text("You do not have permission to run this command.").color(NamedTextColor.RED))
            return true
        }

        // Clean up previous test
        app.activeRegions[sender.uniqueId]?.let { pair ->
            pair.second?.cancel()
            plugin.servicesFwk.chunkBorder.api.destroyRegion(sender.world, pair.first)
            app.activeRegions.remove(sender.uniqueId)
        }
        // The dynamic proximity tracker in WorldBoardTestApp will handle spawning boards!
        
        val targetChunk = sender.location.chunk
        val regionId = "test_region_${sender.uniqueId}"
        
        plugin.servicesFwk.chunkBorder.api.createRegion(regionId, targetChunk.world, targetChunk.x, targetChunk.z).thenAccept { region ->
            sender.sendMessage(Component.text("Test board spawned with an interactive Chunk Border! Punch the border to draw shapes!").color(NamedTextColor.GREEN))

            // Schedule its removal
            val task = plugin.server.globalRegionScheduler.runDelayed(plugin, {
                app.activeRegions.remove(sender.uniqueId)?.let { pair ->
                    plugin.servicesFwk.chunkBorder.api.destroyRegion(sender.world, pair.first)
                }
                app.cleanupPlayer(sender.uniqueId)
            }, 400L) // 20 seconds * 20 ticks

            app.activeRegions[sender.uniqueId] = Pair(region, task)
        }

        return true
    }
}

package site.ftka.survivalcore.apps.RandomSpawn.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.TabCompleter
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.RandomSpawn.RandomSpawnApp
import net.kyori.adventure.text.minimessage.MiniMessage

class RandomSpawnCommand(private val plugin: MClass, private val app: RandomSpawnApp) : CommandExecutor, TabCompleter {
    private val mm = MiniMessage.miniMessage()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("survivalcore.randomspawn")) {
            sender.sendMessage(mm.deserialize("<red>✖ You do not have permission to use this command.</red>"))
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage(mm.deserialize("<gray>Usage: <color:#00ffcc>/randomspawn <analyse <radius> | cancel | randomtp></color></gray>"))
            return true
        }

        when (args[0].lowercase()) {
            "analyse" -> {
                if (args.size < 2) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /randomspawn analyse <radius></red>"))
                    return true
                }
                val radius = args[1].toIntOrNull()
                if (radius == null || radius <= 0) {
                    sender.sendMessage(mm.deserialize("<red>✖ Radius must be a positive integer.</red>"))
                    return true
                }
                plugin.servicesFwk.spawnFinder.algorithm_ss.analyse(sender, radius)
                return true
            }
            "cancel" -> {
                plugin.servicesFwk.spawnFinder.algorithm_ss.cancel(sender)
                return true
            }
            "randomtp" -> {
                if (sender !is Player) {
                    sender.sendMessage(mm.deserialize("<red>✖ Only players can use this command.</red>"))
                    return true
                }
                val spawnFinder = plugin.servicesFwk.spawnFinder
                if (spawnFinder.validSpawns.isEmpty()) {
                    sender.sendMessage(mm.deserialize("<red>✖ No valid spawn chunks available. Try running /randomspawn analyse first.</red>"))
                    return true
                }
                
                // Pick a random valid spawn
                val randomSpawn = spawnFinder.validSpawns.random()
                
                // Teleport to the center of that chunk at the highest block
                val world = sender.world
                val x = randomSpawn.first * 16 + 8
                val z = randomSpawn.second * 16 + 8
                
                plugin.server.regionScheduler.execute(plugin, world, randomSpawn.first, randomSpawn.second) {
                    val y = world.getHighestBlockYAt(x, z)
                    val loc = org.bukkit.Location(world, x.toDouble(), (y + 1).toDouble(), z.toDouble())
                    
                    sender.teleportAsync(loc).thenAccept { success ->
                        if (success) {
                            sender.sendMessage(mm.deserialize("<green>✔ Teleported to random valid spawn chunk (<white>${randomSpawn.first}</white>, <white>${randomSpawn.second}</white>).</green>"))
                        } else {
                            sender.sendMessage(mm.deserialize("<red>✖ Failed to teleport.</red>"))
                        }
                    }
                }
                return true
            }
            else -> {
                sender.sendMessage(mm.deserialize("<red>✖ Unknown subcommand. Usage: /randomspawn <analyse <radius> | cancel | randomtp></red>"))
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        if (!sender.hasPermission("survivalcore.randomspawn")) {
            return emptyList()
        }
        if (args.size == 1) {
            val subcommands = listOf("analyse", "cancel", "randomtp")
            return subcommands.filter { it.startsWith(args[0], ignoreCase = true) }
        }
        if (args.size == 2 && args[0].equals("analyse", ignoreCase = true)) {
            val suggestions = listOf("100", "250", "500", "1000")
            return suggestions.filter { it.startsWith(args[1], ignoreCase = true) }
        }
        return emptyList()
    }
}

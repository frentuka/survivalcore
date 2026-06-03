package site.ftka.survivalcore.apps.SpawnManager.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.TabCompleter
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.command.ConsoleCommandSender
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.SpawnManager.SpawnManagerApp
import net.kyori.adventure.text.minimessage.MiniMessage

class SpawnManagerCommand(private val plugin: MClass, private val app: SpawnManagerApp) : CommandExecutor, TabCompleter {
    private val mm = MiniMessage.miniMessage()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val isAdmin = sender.hasPermission("survivalcore.admin.spawnmanager") || 
                      sender.hasPermission("survivalcore.spawnmanager") || 
                      sender is ConsoleCommandSender || 
                      sender.isOp
                      
        val hasUser = sender.hasPermission("survivalcore.spawnmanager.randomtp")

        if (!isAdmin && !hasUser) {
            sender.sendMessage(mm.deserialize("<red>✖ You do not have permission to use this command.</red>"))
            return true
        }

        if (args.isEmpty() || args[0].equals("help", ignoreCase = true)) {
            sendHelpMenu(sender, isAdmin)
            return true
        }

        when (args[0].lowercase()) {
            "analyse" -> {
                if (!isAdmin) {
                    sender.sendMessage(mm.deserialize("<red>✖ You do not have permission to use this administrative subcommand.</red>"))
                    return true
                }
                if (args.size < 2) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /spawnmanager analyse <radius></red>"))
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
                if (!isAdmin) {
                    sender.sendMessage(mm.deserialize("<red>✖ You do not have permission to use this administrative subcommand.</red>"))
                    return true
                }
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
                    sender.sendMessage(mm.deserialize("<red>✖ No valid spawn chunks available. Try running /spawnmanager analyse first.</red>"))
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
                sender.sendMessage(mm.deserialize("<red>✖ Unknown subcommand. Usage: /spawnmanager <analyse <radius> | cancel | randomtp></red>"))
            }
        }
        return true
    }

    private fun sendHelpMenu(sender: CommandSender, isAdmin: Boolean) {
        sender.sendMessage(mm.deserialize("<gray>--------------------------------------------------</gray>"))
        sender.sendMessage(mm.deserialize("<color:#ffcc00><b>★ SurvivalCore SpawnManager Help ★</b></color>"))
        sender.sendMessage(mm.deserialize("<gray>Manage safe spawn locations and find resource-rich zones.</gray>"))
        sender.sendMessage(mm.deserialize(""))
        
        if (isAdmin) {
            sender.sendMessage(mm.deserialize(" <color:#00ffcc><b>/spawnmanager analyse <radius></b></color>"))
            sender.sendMessage(mm.deserialize("   <gray>»</gray> <white>Scan for valid spawn chunks in a grid.</white>"))
            sender.sendMessage(mm.deserialize(" <color:#00ffcc><b>/spawnmanager cancel</b></color>"))
            sender.sendMessage(mm.deserialize("   <gray>»</gray> <white>Cancel the active spawn chunk scanning process.</white>"))
        }
        
        sender.sendMessage(mm.deserialize(" <color:#00ffcc><b>/spawnmanager randomtp</b></color>"))
        sender.sendMessage(mm.deserialize("   <gray>»</gray> <white>Teleport to a randomly selected, pre-validated safe spawn chunk.</white>"))
        
        sender.sendMessage(mm.deserialize(""))
        sender.sendMessage(mm.deserialize("<gray>Aliases: <white>/sm</white>, <white>/spawn</white>, <white>/randomspawn</white></gray>"))
        sender.sendMessage(mm.deserialize("<gray>--------------------------------------------------</gray>"))
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        val isAdmin = sender.hasPermission("survivalcore.admin.spawnmanager") || 
                      sender.hasPermission("survivalcore.spawnmanager") || 
                      sender is ConsoleCommandSender || 
                      sender.isOp
                      
        val hasUser = sender.hasPermission("survivalcore.spawnmanager.randomtp")

        if (!isAdmin && !hasUser) {
            return emptyList()
        }
        
        if (args.size == 1) {
            val subcommands = if (isAdmin) {
                listOf("analyse", "cancel", "randomtp", "help")
            } else {
                listOf("randomtp", "help")
            }
            return subcommands.filter { it.startsWith(args[0], ignoreCase = true) }
        }
        
        if (args.size == 2 && args[0].equals("analyse", ignoreCase = true) && isAdmin) {
            val suggestions = listOf("100", "250", "500", "1000")
            return suggestions.filter { it.startsWith(args[1], ignoreCase = true) }
        }
        
        return emptyList()
    }
}

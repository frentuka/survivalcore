package site.ftka.survivalcore.apps.ServerAdministration.commands

import site.ftka.survivalcore.apps.ServerAdministration.ServerAdministrationApp
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.ServerAdministration.gui.ServerAdministration_MainMenuGUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.event.HoverEvent

internal class ServerAdministration_serverCommand(private val src: ServerAdministrationApp, private val plugin: MClass): CommandExecutor, TabCompleter {

    /*
        Command: server / sv
     */

    // shortcut vars
    private val essFwk = plugin.essentialsFwk
    private val servFwk = plugin.servicesFwk

    private val SERVER_ADMINISTRATION_PLAYER_PERMISSION = "staff.admin"

    private enum class ServicesEnum {
        CHAT, INVENTORYGUI, LANGUAGE, PERMISSIONS, PLAYERDATA
    }

    private enum class AppsEnum {
        NOTHING, HERE, YET
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is ConsoleCommandSender) { // check authorization
            if (sender !is Player) return false
            if (!servFwk.playerData.data.getPlayerDataMap().containsKey(sender.uniqueId)) {
                sender.sendMessage("§cYour player profile is not currently loaded!")
                return true
            }
            if (!servFwk.permissions.permissions_ss.playerHasPerm_locally(sender.uniqueId, SERVER_ADMINISTRATION_PLAYER_PERMISSION)) {
                val noPermissionMessage = plugin.servicesFwk.language.api.playerLanguagePack(sender.uniqueId).command_error_player_noPermission
                sender.sendMessage(noPermissionMessage)
                return true
            }
        }

        if (args.isEmpty()) {
            if (sender is Player) {
                val gui = ServerAdministration_MainMenuGUI(plugin, sender)
                sender.openInventory(gui.inventory)
                return true
            } else {
                sender.sendMessage(src.lang.help_message)
                return true
            }
        }

        if (args.get(0).equals("logs", ignoreCase = true)) {
            if (args.size < 2) {
                sender.sendMessage(Component.text("Usage: /server logs <loggerNames> [level] [date] [limit]").color(NamedTextColor.RED))
                return true
            }
            val loggerNames = args[1].split(",")
            val levelFilter = if (args.size > 2) args[2] else "all"
            val dateFilter = if (args.size > 3) args[3] else "now"
            val limit = if (args.size > 4) args[4].toIntOrNull() ?: 10 else 10

            plugin.globalScheduler.execute {
                val allLogs = mutableListOf<Pair<String, site.ftka.survivalcore.initless.logging.objects.Log>>()
                
                val colors = listOf(NamedTextColor.AQUA, NamedTextColor.GREEN, NamedTextColor.LIGHT_PURPLE, NamedTextColor.GOLD, NamedTextColor.BLUE, NamedTextColor.DARK_AQUA, NamedTextColor.DARK_GREEN)
                val moduleColorMap = mutableMapOf<String, NamedTextColor>()
                var colorIndex = 0
                
                for (loggerName in loggerNames) {
                    if (loggerName.isBlank()) continue
                    val logs = plugin.loggingInitless.queryLogs(loggerName, levelFilter, dateFilter, limit)
                    
                    if (logs.isNotEmpty() && !moduleColorMap.containsKey(loggerName)) {
                        moduleColorMap[loggerName] = colors[colorIndex % colors.size]
                        colorIndex++
                    }
                    logs.forEach { allLogs.add(Pair(loggerName, it)) }
                }
                
                // Sort by timestamp descending to get newest logs first across all modules
                val finalLogs = allLogs.sortedByDescending { it.second.timestamp }.take(limit)

                val header = Component.text()
                    .append(Component.text("\n                                        \n").color(NamedTextColor.DARK_GRAY).decorate(net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH))
                    .append(Component.text("  Logs Query Results\n").color(NamedTextColor.GOLD).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD))
                    .append(Component.text("  Modules: ").color(NamedTextColor.GRAY))
                    .append(Component.text("${args[1].replace(",", ", ")}\n").color(NamedTextColor.WHITE))
                    .append(Component.text("  Level: ").color(NamedTextColor.GRAY))
                    .append(Component.text(levelFilter).color(NamedTextColor.WHITE))
                    .append(Component.text("  |  ").color(NamedTextColor.DARK_GRAY))
                    .append(Component.text("Date: ").color(NamedTextColor.GRAY))
                    .append(Component.text("$dateFilter\n").color(NamedTextColor.WHITE))
                    .append(Component.text("                                        ").color(NamedTextColor.DARK_GRAY).decorate(net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH))
                    .build()
                sender.sendMessage(header)

                if (finalLogs.isEmpty()) {
                    sender.sendMessage(Component.text("No logs found.").color(NamedTextColor.RED))
                    sender.sendMessage(Component.text("                                        ").color(NamedTextColor.DARK_GRAY).decorate(net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH))
                } else {
                    val sdfUTC = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    sdfUTC.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    
                    // Reverse to print the oldest logs at the top and newest at the bottom
                    finalLogs.reversed().forEach { pair ->
                        val loggerName = pair.first
                        val log = pair.second
                        val modColor = moduleColorMap[loggerName] ?: NamedTextColor.GRAY
                        
                        // Extract uppercase letters for the shortname. Fallback to first 2 letters if none found.
                        val shortNameRaw = loggerName.filter { it.isUpperCase() }
                        val shortName = if (shortNameRaw.isNotEmpty()) shortNameRaw else loggerName.take(2).uppercase()
                        
                        val timeStr = site.ftka.survivalcore.utils.dateUtils.timeFormat(log.timestamp, "HH:mm:ss")
                        val hoverTimeText = Component.text("UTC: ${sdfUTC.format(java.util.Date(log.timestamp))}\nEpoch: ${log.timestamp}").color(NamedTextColor.GRAY)
                        
                        val timeComp = Component.text("[$timeStr]")
                            .color(NamedTextColor.DARK_GRAY)
                            .hoverEvent(HoverEvent.showText(hoverTimeText))
                        
                        val levelColor = when (log.level) {
                            site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel.DEBUG -> NamedTextColor.GRAY
                            site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel.NORMAL -> NamedTextColor.BLUE
                            site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel.LOW -> NamedTextColor.YELLOW
                            site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel.HIGH -> NamedTextColor.RED
                        }
                        
                        val levelComp = Component.text(" [${log.level}] ")
                            .color(levelColor)
                            
                        val modComp = Component.text("[$shortName] ")
                            .color(modColor)
                            .hoverEvent(HoverEvent.showText(Component.text("Module: $loggerName").color(modColor)))
                            
                        val finalMsg = Component.text()
                            .append(timeComp)
                            .append(levelComp)
                            .append(modComp)
                            .append(log.text)
                            .build()
                            
                        sender.sendMessage(finalMsg)
                    }
                    sender.sendMessage(Component.text("                                        ").color(NamedTextColor.DARK_GRAY).decorate(net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH))
                }
            }
            return true
        }

        if (args.get(0) == "service") return false


        return false
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): List<String> {
        val completions = mutableListOf<String>()

        if (args.size == 1) { // /server [app/service/logs]
            val options = listOf("app", "service", "logs")
            completions.addAll(options.filter { it.startsWith(args[0], ignoreCase = true) })
        } else if (args.size == 2) {
            when (args[0].lowercase()) {
                "app" -> {
                    val options = AppsEnum.entries.map { it.name.lowercase() }
                    completions.addAll(options.filter { it.startsWith(args[1], ignoreCase = true) })
                }
                "service" -> {
                    val options = ServicesEnum.entries.map { it.name.lowercase() }
                    completions.addAll(options.filter { it.startsWith(args[1], ignoreCase = true) })
                }
                "logs" -> {
                    // Argument is comma separated list of loggers
                    val logsFolder = plugin.loggingInitless.logsFolder
                    if (logsFolder.exists() && logsFolder.isDirectory) {
                        val loggerNames = logsFolder.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: emptyList()
                        
                        val currentArg = args[1]
                        val parts = currentArg.split(",")
                        val prefix = if (parts.size > 1) {
                            parts.dropLast(1).joinToString(",") + ","
                        } else {
                            ""
                        }
                        val partialLogger = parts.last()
                        
                        val alreadySelected = parts.dropLast(1).map { it.lowercase() }
                        val filteredLoggerNames = loggerNames.filter { it.lowercase() !in alreadySelected }
                        
                        completions.addAll(filteredLoggerNames.filter { it.startsWith(partialLogger, ignoreCase = true) }.map { prefix + it })
                    }
                }
            }
        } else if (args.size == 3) {
            when (args[0].lowercase()) {
                "app", "service" -> {
                    val options = listOf("reload")
                    completions.addAll(options.filter { it.startsWith(args[2], ignoreCase = true) })
                }
                "logs" -> {
                    val options = listOf("all", "info", "warning", "error", "debug")
                    completions.addAll(options.filter { it.startsWith(args[2], ignoreCase = true) })
                }
            }
        } else if (args.size == 4) {
            if (args[0].equals("logs", ignoreCase = true)) {
                val options = mutableListOf("now")
                val loggerNames = args[1].split(",")
                for (loggerName in loggerNames) {
                    if (loggerName.isBlank()) continue
                    val loggerDir = java.io.File(plugin.loggingInitless.logsFolder, loggerName)
                    if (loggerDir.exists() && loggerDir.isDirectory) {
                        val dates = loggerDir.listFiles()?.filter { it.isFile }
                            ?.mapNotNull { file ->
                                val match = Regex("""(\d{4}-\d{2}-\d{2})""").find(file.name)
                                match?.value
                            } ?: emptyList()
                        options.addAll(dates)
                    }
                }
                val distinctDates = options.distinct()
                completions.addAll(distinctDates.filter { it.startsWith(args[3], ignoreCase = true) })
            }
        } else if (args.size == 5) {
            if (args[0].equals("logs", ignoreCase = true)) {
                val options = listOf("10", "20", "50", "100")
                completions.addAll(options.filter { it.startsWith(args[4], ignoreCase = true) })
            }
        }
        return completions
    }

}
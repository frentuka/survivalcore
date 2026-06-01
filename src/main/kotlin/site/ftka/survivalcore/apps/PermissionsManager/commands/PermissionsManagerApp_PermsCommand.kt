package site.ftka.survivalcore.apps.PermissionsManager.commands

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.TabCompleter
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.PermissionsManager.PermissionsManagerApp
import site.ftka.survivalcore.apps.PermissionsManager.gui.PermissionsManager_GroupsListGUI
import site.ftka.survivalcore.services.permissions.PermissionsAPI
import site.ftka.survivalcore.services.permissions.subservices.PermissionsService_PlayersSubservice.*
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.*
import java.util.concurrent.CompletableFuture

internal class PermissionsManagerApp_PermsCommand(private val app: PermissionsManagerApp, private val plugin: MClass) : CommandExecutor, TabCompleter {

    private val mm = MiniMessage.miniMessage()
    private val permsAPI: PermissionsAPI
        get() = plugin.servicesFwk.permissions.api

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        val isPlayer = sender is Player

        // Permission check for players
        if (isPlayer) {
            val player = sender as Player
            if (!player.hasPermission("survivalcore.admin.permissions") && !player.isOp) {
                player.sendMessage(mm.deserialize("<red>✖ You do not have permission to manage permissions.</red>"))
                return true
            }
        }

        // GUI / Help - No args
        if (args.isEmpty()) {
            if (isPlayer) {
                val player = sender as Player
                plugin.server.regionScheduler.execute(plugin, player.world, player.location.chunk.x, player.location.chunk.z) {
                    val gui = PermissionsManager_GroupsListGUI(plugin, player)
                    player.openInventory(gui.inventory)
                }
            } else {
                sendHelpMenu(sender)
            }
            return true
        }

        // Global Subcommands (info)
        if (args[0].lowercase() == "info" && args.size >= 2) {
            val targetName = args[1]
            val targetUUID = plugin.essentialsFwk.usernameTracker.getUUID(targetName)
            if (targetUUID == null) {
                sender.sendMessage(mm.deserialize("<red>✖ Player '$targetName' has never played on this server.</red>"))
                return true
            }

            sender.sendMessage(mm.deserialize("<yellow>Retrieving permission details for player '$targetName'... Please wait.</yellow>"))
            scope.launch {
                val groupsFuture = permsAPI.player_getGroups(targetUUID)
                val permsFuture = permsAPI.player_getPerms(targetUUID)
                val displayGroupFuture = permsAPI.player_getDisplayGroup(targetUUID)

                CompletableFuture.allOf(groupsFuture, permsFuture, displayGroupFuture).thenAccept {
                    val groupUUIDs = groupsFuture.get() ?: emptySet()
                    val directPerms = permsFuture.get() ?: emptySet()
                    val displayGroupUUID = displayGroupFuture.get()

                    val groupNames = groupUUIDs.mapNotNull { permsAPI.getGroup(it)?.name }
                    val displayGroupName = displayGroupUUID?.let { permsAPI.getGroup(it)?.name } ?: "None"

                    sender.sendMessage(mm.deserialize("<gray>--------------------------------------------------</gray>"))
                    sender.sendMessage(mm.deserialize("<color:#ffcc00><b>★ Player Permissions Info: <white>$targetName</white> ★</b></color>"))
                    sender.sendMessage(mm.deserialize("<gray>UUID: <white>$targetUUID</white></gray>"))
                    sender.sendMessage(mm.deserialize("<gray>Primary Display Group: <gold>$displayGroupName</gold></gray>"))
                    sender.sendMessage(mm.deserialize("<gray>Groups Joined: <white>${if (groupNames.isEmpty()) "None" else groupNames.joinToString(", ")}</white></gray>"))
                    sender.sendMessage(mm.deserialize("<gray>Direct Permissions: <white>${directPerms.size} nodes</white></gray>"))
                    if (directPerms.isNotEmpty()) {
                        directPerms.sorted().forEach { sender.sendMessage(mm.deserialize("<gray> - <white>$it</white></gray>")) }
                    }

                    // Active/Inherited Permissions
                    val activePerms = plugin.servicesFwk.permissions.permissions_ss.playerPerms(targetUUID)
                    sender.sendMessage(mm.deserialize("<gray>Total Active Perms (Direct + Inherited): <white>${activePerms.size} nodes</white></gray>"))
                    sender.sendMessage(mm.deserialize("<gray>--------------------------------------------------</gray>"))
                }
            }
            return true
        }

        // Subcommands related to player - /perms <player> <subcommand> <args>
        val targetName = args[0]
        val targetUUID = plugin.essentialsFwk.usernameTracker.getUUID(targetName)
        if (targetUUID == null) {
            sender.sendMessage(mm.deserialize("<red>✖ Player '$targetName' has never played on this server.</red>"))
            return true
        }

        if (args.size == 1) {
            if (isPlayer) {
                // Keep default behavior: open player_groups screen
                sender.sendMessage(mm.deserialize("<yellow>To manage player groups visually, use the command options below or PlayerDataManager GUI.</yellow>"))
            }
            sendHelpMenu(sender)
            return true
        }

        val subcommand = args[1].lowercase()

        when (subcommand) {
            "info" -> {
                // Alias to /perms info <player>
                sender.sendMessage(mm.deserialize("<yellow>Retrieving permission details for player '$targetName'... Please wait.</yellow>"))
                scope.launch {
                    val groupsFuture = permsAPI.player_getGroups(targetUUID)
                    val permsFuture = permsAPI.player_getPerms(targetUUID)
                    val displayGroupFuture = permsAPI.player_getDisplayGroup(targetUUID)

                    CompletableFuture.allOf(groupsFuture, permsFuture, displayGroupFuture).thenAccept {
                        val groupUUIDs = groupsFuture.get() ?: emptySet()
                        val directPerms = permsFuture.get() ?: emptySet()
                        val displayGroupUUID = displayGroupFuture.get()

                        val groupNames = groupUUIDs.mapNotNull { permsAPI.getGroup(it)?.name }
                        val displayGroupName = displayGroupUUID?.let { permsAPI.getGroup(it)?.name } ?: "None"

                        sender.sendMessage(mm.deserialize("<gray>--------------------------------------------------</gray>"))
                        sender.sendMessage(mm.deserialize("<color:#ffcc00><b>★ Player Permissions Info: <white>$targetName</white> ★</b></color>"))
                        sender.sendMessage(mm.deserialize("<gray>UUID: <white>$targetUUID</white></gray>"))
                        sender.sendMessage(mm.deserialize("<gray>Primary Display Group: <gold>$displayGroupName</gold></gray>"))
                        sender.sendMessage(mm.deserialize("<gray>Groups Joined: <white>${if (groupNames.isEmpty()) "None" else groupNames.joinToString(", ")}</white></gray>"))
                        sender.sendMessage(mm.deserialize("<gray>Direct Permissions: <white>${directPerms.size} nodes</white></gray>"))
                        if (directPerms.isNotEmpty()) {
                            directPerms.sorted().forEach { sender.sendMessage(mm.deserialize("<gray> - <white>$it</white></gray>")) }
                        }

                        // Active/Inherited Permissions
                        val activePerms = plugin.servicesFwk.permissions.permissions_ss.playerPerms(targetUUID)
                        sender.sendMessage(mm.deserialize("<gray>Total Active Perms (Direct + Inherited): <white>${activePerms.size} nodes</white></gray>"))
                        sender.sendMessage(mm.deserialize("<gray>--------------------------------------------------</gray>"))
                    }
                }
            }
            "setdisplaygroup" -> {
                if (args.size < 3) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /perms $targetName setdisplaygroup <group></red>"))
                    return true
                }
                scope.launch {
                    val result = permsAPI.player_setDisplayGroup(targetUUID, args[2])
                    if (result == Permissions_setDisplayGroupResult.SUCCESS) {
                        sender.sendMessage(mm.deserialize("<green>✔ Successfully set display group for player '$targetName' to '${args[2]}'.</green>"))
                    } else {
                        sender.sendMessage(mm.deserialize("<red>✖ Failed to update display group. Verify group exists.</red>"))
                    }
                }
            }
            "addgroup" -> {
                if (args.size < 3) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /perms $targetName addgroup <group></red>"))
                    return true
                }
                scope.launch {
                    val result = permsAPI.player_addGroup(targetUUID, args[2])
                    when (result) {
                        Permissions_addGroupResult.SUCCESS -> {
                            sender.sendMessage(mm.deserialize("<green>✔ Successfully added player '$targetName' to group '${args[2]}'.</green>"))
                        }
                        Permissions_addGroupResult.FAILURE_PLAYER_ALREADY_IN_GROUP -> {
                            sender.sendMessage(mm.deserialize("<red>✖ Player '$targetName' is already in group '${args[2]}'.</red>"))
                        }
                        Permissions_addGroupResult.FAILURE_GROUP_UNAVAILABLE -> {
                            sender.sendMessage(mm.deserialize("<red>✖ Group '${args[2]}' does not exist.</red>"))
                        }
                        else -> sender.sendMessage(mm.deserialize("<red>✖ Failed to add player to group.</red>"))
                    }
                }
            }
            "removegroup" -> {
                if (args.size < 3) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /perms $targetName removegroup <group></red>"))
                    return true
                }
                scope.launch {
                    val result = permsAPI.player_removeGroup(targetUUID, args[2])
                    when (result) {
                        Permissions_removeGroupResult.SUCCESS -> {
                            sender.sendMessage(mm.deserialize("<green>✔ Successfully removed player '$targetName' from group '${args[2]}'.</green>"))
                        }
                        Permissions_removeGroupResult.FAILURE_PLAYER_NOT_IN_GROUP -> {
                            sender.sendMessage(mm.deserialize("<red>✖ Player '$targetName' is not in group '${args[2]}'.</red>"))
                        }
                        else -> sender.sendMessage(mm.deserialize("<red>✖ Failed to remove player from group.</red>"))
                    }
                }
            }
            "addperm" -> {
                if (args.size < 3) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /perms $targetName addperm <permission></red>"))
                    return true
                }
                scope.launch {
                    val result = permsAPI.player_addPerm(targetUUID, args[2])
                    when (result) {
                        Permissions_addPermissionResult.SUCCESS -> {
                            sender.sendMessage(mm.deserialize("<green>✔ Successfully added permission '${args[2]}' to player '$targetName'.</green>"))
                        }
                        Permissions_addPermissionResult.FAILURE_PLAYER_ALREADY_HAS_PERMISSION -> {
                            sender.sendMessage(mm.deserialize("<red>✖ Player '$targetName' already possesses direct permission '${args[2]}'.</red>"))
                        }
                        else -> sender.sendMessage(mm.deserialize("<red>✖ Failed to add permission to player.</red>"))
                    }
                }
            }
            "removeperm" -> {
                if (args.size < 3) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /perms $targetName removeperm <permission></red>"))
                    return true
                }
                scope.launch {
                    val result = permsAPI.player_removePerm(targetUUID, args[2])
                    when (result) {
                        Permissions_removePermissionResult.SUCCESS -> {
                            sender.sendMessage(mm.deserialize("<green>✔ Successfully removed permission '${args[2]}' from player '$targetName'.</green>"))
                        }
                        Permissions_removePermissionResult.FAILURE_PLAYER_DOESNT_HAVE_PERMISSION -> {
                            sender.sendMessage(mm.deserialize("<red>✖ Player '$targetName' does not possess direct permission '${args[2]}'.</red>"))
                        }
                        else -> sender.sendMessage(mm.deserialize("<red>✖ Failed to remove permission from player.</red>"))
                    }
                }
            }
            else -> {
                sender.sendMessage(mm.deserialize("<red>✖ Unknown subcommand. Use addgroup, removegroup, addperm, removeperm, setdisplaygroup, or info.</red>"))
            }
        }

        return true
    }

    private fun sendHelpMenu(sender: CommandSender) {
        sender.sendMessage(mm.deserialize("<gray>--------------------------------------------------</gray>"))
        sender.sendMessage(mm.deserialize("<color:#ffcc00><b>★ SurvivalCore Permissions Manager — Players ★</b></color>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/perms info <player></color> <gray>»</gray> <white>Inspect player groups & direct permissions.</white>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/perms <player> addgroup <group></color> <gray>»</gray> <white>Add player to a group.</white>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/perms <player> removegroup <group></color> <gray>»</gray> <white>Remove player from a group.</white>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/perms <player> setdisplaygroup <group></color> <gray>»</gray> <white>Set player's display tag group.</white>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/perms <player> addperm <perm></color> <gray>»</gray> <white>Assign direct player permission.</white>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/perms <player> removeperm <perm></color> <gray>»</gray> <white>Remove direct player permission.</white>"))
        sender.sendMessage(mm.deserialize("<gray>--------------------------------------------------</gray>"))
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
        if (!sender.hasPermission("survivalcore.admin.permissions") && !sender.isOp && sender !is ConsoleCommandSender) {
            return emptyList()
        }

        val loadedGroups = permsAPI.getGroups().map { it.name }
        val onlinePlayers = plugin.server.onlinePlayers.map { it.name }
        
        if (args.size == 1) {
            val suggestions = listOf("info") + onlinePlayers
            return suggestions.filter { it.startsWith(args[0], ignoreCase = true) }
        }

        if (args.size == 2) {
            if (args[0].lowercase() == "info") {
                val registeredPlayers = plugin.essentialsFwk.usernameTracker.usernameDatabase().getMap().values.toList()
                return registeredPlayers.filter { it.startsWith(args[1], ignoreCase = true) }
            }
            
            val targetUUID = plugin.essentialsFwk.usernameTracker.getUUID(args[0])
            if (targetUUID != null) {
                val subcommands = listOf("addgroup", "removegroup", "addperm", "removeperm", "setdisplaygroup", "info")
                return subcommands.filter { it.startsWith(args[1], ignoreCase = true) }
            }
        }

        if (args.size == 3) {
            val targetUUID = plugin.essentialsFwk.usernameTracker.getUUID(args[0])
            if (targetUUID != null) {
                when (args[1].lowercase()) {
                    "setdisplaygroup" -> {
                        return loadedGroups.filter { it.startsWith(args[2], ignoreCase = true) }
                    }
                    "addgroup" -> {
                        val playerGroupsFuture = permsAPI.player_getGroups(targetUUID, localOnly = true)
                        val joinedGroups = playerGroupsFuture.get()?.mapNotNull { permsAPI.getGroup(it)?.name } ?: emptyList()
                        return loadedGroups.filter { it !in joinedGroups && it.startsWith(args[2], ignoreCase = true) }
                    }
                    "removegroup" -> {
                        val playerGroupsFuture = permsAPI.player_getGroups(targetUUID, localOnly = true)
                        val joinedGroups = playerGroupsFuture.get()?.mapNotNull { permsAPI.getGroup(it)?.name } ?: emptyList()
                        return joinedGroups.filter { it.startsWith(args[2], ignoreCase = true) }
                    }
                    "removeperm" -> {
                        val playerPermsFuture = permsAPI.player_getPerms(targetUUID, localOnly = true)
                        val directPerms = playerPermsFuture.get() ?: emptySet()
                        return directPerms.filter { it.startsWith(args[2], ignoreCase = true) }
                    }
                    "addperm" -> {
                        val commonPerms = listOf(
                            "survivalcore.admin", "survivalcore.staff", "survivalcore.spawnmanager",
                            "survivalcore.spawnmanager.randomtp", "survivalcore.admin.permissions",
                            "minecraft.command.teleport", "minecraft.command.gamemode"
                        )
                        return commonPerms.filter { it.startsWith(args[2], ignoreCase = true) }
                    }
                }
            }
        }

        return emptyList()
    }
}
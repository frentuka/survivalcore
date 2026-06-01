package site.ftka.survivalcore.apps.PermissionsManager.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.TabCompleter
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.PermissionsManager.PermissionsManagerApp
import site.ftka.survivalcore.apps.PermissionsManager.gui.PermissionsManager_GroupsListGUI
import site.ftka.survivalcore.apps.PermissionsManager.gui.PermissionsManager_GroupDetailGUI
import site.ftka.survivalcore.services.permissions.PermissionsAPI
import site.ftka.survivalcore.services.permissions.subservices.PermissionsService_GroupsSubservice.*
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.format.NamedTextColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.CompletableFuture

internal class PermissionsManagerApp_GroupsCommand(private val app: PermissionsManagerApp, private val plugin: MClass) : CommandExecutor, TabCompleter {

    private val mm = MiniMessage.miniMessage()
    private val permsAPI: PermissionsAPI
        get() = plugin.servicesFwk.permissions.api

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        // Permission check
        if (!sender.hasPermission("survivalcore.admin.permissions") && !sender.isOp && sender !is ConsoleCommandSender) {
            sender.sendMessage(mm.deserialize("<red>✖ You do not have permission to manage permissions.</red>"))
            return true
        }

        // GUI or Help - /groups without args
        if (args.isEmpty()) {
            if (sender is Player) {
                plugin.server.regionScheduler.execute(plugin, sender.world, sender.location.chunk.x, sender.location.chunk.z) {
                    val gui = PermissionsManager_GroupsListGUI(plugin, sender)
                    sender.openInventory(gui.inventory)
                }
            } else {
                sendHelpMenu(sender)
            }
            return true
        }

        // Global Subcommands (create, delete, rename, list, info)
        when (args[0].lowercase()) {
            "help" -> {
                sendHelpMenu(sender)
                return true
            }
            "list" -> {
                val groups = permsAPI.getGroups().sortedBy { it.name }
                sender.sendMessage(mm.deserialize("<gray>--------------------------------------------------</gray>"))
                sender.sendMessage(mm.deserialize("<color:#ffcc00><b>★ SurvivalCore Permission Groups (${groups.size}) ★</b></color>"))
                for (group in groups) {
                    sender.sendMessage(mm.deserialize(" <gray>•</gray> <${group.primaryColor}>${group.displayName}</${group.primaryColor}> <gray>(System ID: <white>${group.name}</white> | Tag: <white>${group.tag}</white>)</gray>"))
                }
                sender.sendMessage(mm.deserialize("<gray>--------------------------------------------------</gray>"))
                return true
            }
            "create" -> {
                if (args.size < 2) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /groups create <name></red>"))
                    return true
                }
                val newGroup = permsAPI.createGroup(args[1])
                if (newGroup != null) {
                    sender.sendMessage(mm.deserialize("<green>✔ Successfully created group <white>${newGroup.displayName}</white>.</green>"))
                } else {
                    sender.sendMessage(mm.deserialize("<red>✖ Failed: Group '${args[1]}' already exists.</red>"))
                }
                return true
            }
            "delete" -> {
                if (args.size < 2) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /groups delete <name></red>"))
                    return true
                }
                val success = permsAPI.deleteGroup(args[1])
                if (success) {
                    sender.sendMessage(mm.deserialize("<green>✔ Successfully deleted group <white>${args[1]}</white>.</green>"))
                } else {
                    sender.sendMessage(mm.deserialize("<red>✖ Failed: Group '${args[1]}' does not exist.</red>"))
                }
                return true
            }
            "rename" -> {
                if (args.size < 3) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /groups rename <oldName> <newName></red>"))
                    return true
                }
                val group = permsAPI.getGroup(args[1])
                if (group == null) {
                    sender.sendMessage(mm.deserialize("<red>✖ Group '${args[1]}' does not exist.</red>"))
                    return true
                }
                permsAPI.renameGroup(args[1], args[2])
                sender.sendMessage(mm.deserialize("<green>✔ Successfully renamed group <white>${args[1]}</white> to <white>${args[2]}</white>.</green>"))
                return true
            }
        }

        // Check if command is /groups info <group> [inheritances|members|onlineMembers|perms]
        var infoGroup: String? = null
        var infoSection: String? = null

        if (args[0].equals("info", ignoreCase = true) && args.size >= 2) {
            infoGroup = args[1]
            if (args.size >= 3) infoSection = args[2].lowercase()
        } else if (args.size >= 2 && args[1].equals("info", ignoreCase = true)) {
            infoGroup = args[0]
            if (args.size >= 3) infoSection = args[2].lowercase()
        }

        if (infoGroup != null) {
            val group = permsAPI.getGroup(infoGroup)
            if (group == null) {
                sender.sendMessage(mm.deserialize("<red>✖ Group '$infoGroup' does not exist.</red>"))
                return true
            }

            if (infoSection == null) {
                sender.sendMessage(mm.deserialize("<gray>--------------------------------------------------</gray>"))
                sender.sendMessage(mm.deserialize("<color:#ffcc00><b>★ Group Details: <${group.primaryColor}>${group.displayName}</${group.primaryColor}> ★</b></color>"))
                sender.sendMessage(mm.deserialize("<gray>System ID: <white>${group.name}</white></gray>"))
                sender.sendMessage(mm.deserialize("<gray>Tag Prefix: <white>${group.tag}</white></gray>"))
                sender.sendMessage(mm.deserialize("<gray>Category: <white>${group.category.name}</white></gray>"))
                sender.sendMessage(mm.deserialize("<gray>Colors: <${group.primaryColor}>Primary</${group.primaryColor}> / <${group.secondaryColor}>Secondary</${group.secondaryColor}></gray>"))

                val inhNames = group.inheritances.mapNotNull { permsAPI.getGroup(it)?.name }
                sender.sendMessage(mm.deserialize("<gray>Inherits From: <white>${if (inhNames.isEmpty()) "None" else inhNames.joinToString(", ")}</white></gray>"))
                sender.sendMessage(mm.deserialize("<gray>Direct Permissions: <white>${group.perms.size} nodes</white></gray>"))
                sender.sendMessage(mm.deserialize("<gray><i>Usage: /groups ${group.name} info [inheritances|members|onlineMembers|perms]</i></gray>"))
                sender.sendMessage(mm.deserialize("<gray>--------------------------------------------------</gray>"))
            } else {
                when (infoSection) {
                    "inheritances", "inh" -> {
                        val inhNames = group.inheritances.mapNotNull { permsAPI.getGroup(it)?.name }
                        sender.sendMessage(mm.deserialize("<gray>Group <gold>${group.name}</gold> inherits from <white>${inhNames.size}</white> groups:</gray>"))
                        if (inhNames.isEmpty()) sender.sendMessage(mm.deserialize("<gray> - None</gray>"))
                        inhNames.forEach { sender.sendMessage(mm.deserialize("<gray> - <yellow>$it</yellow></gray>")) }
                    }
                    "perms", "permissions" -> {
                        sender.sendMessage(mm.deserialize("<gray>Group <gold>${group.name}</gold> has <white>${group.perms.size}</white> direct permissions:</gray>"))
                        if (group.perms.isEmpty()) sender.sendMessage(mm.deserialize("<gray> - None</gray>"))
                        group.perms.sorted().forEach { sender.sendMessage(mm.deserialize("<gray> - <white>$it</white></gray>")) }
                    }
                    "onlinemembers", "online" -> {
                        val onlineMembers = plugin.server.onlinePlayers.filter { player ->
                            val groups = permsAPI.player_getGroups(player.uniqueId, localOnly = true).get()
                            groups != null && groups.contains(group.uuid)
                        }
                        sender.sendMessage(mm.deserialize("<gray>Group <gold>${group.name}</gold> has <white>${onlineMembers.size}</white> online members:</gray>"))
                        if (onlineMembers.isEmpty()) sender.sendMessage(mm.deserialize("<gray> - None</gray>"))
                        onlineMembers.forEach { sender.sendMessage(mm.deserialize("<gray> - <green>${it.name}</green></gray>")) }
                    }
                    "members", "allmembers" -> {
                        sender.sendMessage(mm.deserialize("<yellow>Scanning database for all members of group '${group.name}'... Please wait.</yellow>"))
                        scope.launch {
                            val membersList = mutableListOf<String>()
                            val futures = plugin.essentialsFwk.usernameTracker.usernameDatabase().getMap().map { (uuid, name) ->
                                permsAPI.player_getGroups(uuid).thenAccept { groups ->
                                    if (groups != null && groups.contains(group.uuid)) {
                                        synchronized(membersList) {
                                            membersList.add(name)
                                        }
                                    }
                                }
                            }
                            CompletableFuture.allOf(*futures.toTypedArray()).thenRun {
                                sender.sendMessage(mm.deserialize("<gray>Group <gold>${group.name}</gold> has <white>${membersList.size}</white> total registered members:</gray>"))
                                if (membersList.isEmpty()) sender.sendMessage(mm.deserialize("<gray> - None</gray>"))
                                membersList.sorted().forEach { sender.sendMessage(mm.deserialize("<gray> - <white>$it</white></gray>")) }
                            }
                        }
                    }
                    else -> {
                        sender.sendMessage(mm.deserialize("<red>✖ Unknown info section. Use inheritances, perms, onlineMembers, or members.</red>"))
                    }
                }
            }
            return true
        }

        // Group operations: /groups {group} <subcommand> <param>
        val targetGroup = permsAPI.getGroup(args[0])
        if (targetGroup == null) {
            sender.sendMessage(mm.deserialize("<red>✖ Group '${args[0]}' or subcommand '${args[0]}' does not exist.</red>"))
            return true
        }

        if (args.size == 1) {
            if (sender is Player) {
                plugin.server.regionScheduler.execute(plugin, sender.world, sender.location.chunk.x, sender.location.chunk.z) {
                    val gui = PermissionsManager_GroupDetailGUI(plugin, sender, targetGroup.uuid)
                    sender.openInventory(gui.inventory)
                }
            } else {
                sender.sendMessage(mm.deserialize("<red>✖ Console cannot open GUIs. Use `/groups ${targetGroup.name} info` to see data.</red>"))
            }
            return true
        }

        val subcommand = args[1].lowercase()

        when (subcommand) {
            "settag" -> {
                if (args.size < 3) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /groups ${targetGroup.name} settag <tag></red>"))
                    return true
                }
                permsAPI.setTagToGroup(targetGroup.uuid, args[2])
                sender.sendMessage(mm.deserialize("<green>✔ Tag prefix for group <white>${targetGroup.name}</white> updated to <white>${args[2]}</white>.</green>"))
            }
            "setcolor" -> {
                if (args.size < 3) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /groups ${targetGroup.name} setcolor <primary> [secondary]</red>"))
                    return true
                }
                permsAPI.setPrimaryColorToGroup(targetGroup.uuid, args[2])
                if (args.size >= 4) {
                    permsAPI.setSecondaryColorToGroup(targetGroup.uuid, args[3])
                }
                sender.sendMessage(mm.deserialize("<green>✔ Colors for group <white>${targetGroup.name}</white> successfully updated.</green>"))
            }
            "addperm" -> {
                if (args.size < 3) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /groups ${targetGroup.name} addperm <permission></red>"))
                    return true
                }
                val result = permsAPI.group_addPerm(targetGroup.name, args[2])
                when (result) {
                    PermissionGroup_addPermissionResult.SUCCESS -> {
                        sender.sendMessage(mm.deserialize("<green>✔ Added permission '${args[2]}' to group '${targetGroup.name}'.</green>"))
                    }
                    PermissionGroup_addPermissionResult.FAILURE_PERMISSION_ALREADY_EXISTS -> {
                        sender.sendMessage(mm.deserialize("<red>✖ Group '${targetGroup.name}' already possesses permission '${args[2]}'.</red>"))
                    }
                    else -> sender.sendMessage(mm.deserialize("<red>✖ An error occurred while adding the permission.</red>"))
                }
            }
            "removeperm" -> {
                if (args.size < 3) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /groups ${targetGroup.name} removeperm <permission></red>"))
                    return true
                }
                val result = permsAPI.group_removePerm(targetGroup.name, args[2])
                when (result) {
                    PermissionGroup_removePermissionResult.SUCCESS -> {
                        sender.sendMessage(mm.deserialize("<green>✔ Removed permission '${args[2]}' from group '${targetGroup.name}'.</green>"))
                    }
                    PermissionGroup_removePermissionResult.FAILURE_PERMISSION_DOES_NOT_EXIST -> {
                        sender.sendMessage(mm.deserialize("<red>✖ Group '${targetGroup.name}' does not possess permission '${args[2]}'.</red>"))
                    }
                    else -> sender.sendMessage(mm.deserialize("<red>✖ An error occurred while removing the permission.</red>"))
                }
            }
            "addinh", "addinheritance" -> {
                if (args.size < 3) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /groups ${targetGroup.name} addinh <inheritedGroup></red>"))
                    return true
                }
                val result = permsAPI.group_addInheritance(targetGroup.name, args[2])
                when (result) {
                    PermissionGroup_addInheritanceResult.SUCCESS -> {
                        sender.sendMessage(mm.deserialize("<green>✔ Group '${targetGroup.name}' now inherits from '${args[2]}'.</green>"))
                    }
                    PermissionGroup_addInheritanceResult.FAILURE_INHERITANCE_ALREADY_SET -> {
                        sender.sendMessage(mm.deserialize("<red>✖ Group '${targetGroup.name}' already inherits from '${args[2]}'.</red>"))
                    }
                    PermissionGroup_addInheritanceResult.FAILURE_INHERITANCE_GROUP_DOES_NOT_EXIST -> {
                        sender.sendMessage(mm.deserialize("<red>✖ Inherited group '${args[2]}' does not exist.</red>"))
                    }
                    else -> sender.sendMessage(mm.deserialize("<red>✖ An error occurred while setting inheritance.</red>"))
                }
            }
            "removeinh", "removeinheritance" -> {
                if (args.size < 3) {
                    sender.sendMessage(mm.deserialize("<red>✖ Usage: /groups ${targetGroup.name} removeinh <inheritedGroup></red>"))
                    return true
                }
                val result = permsAPI.group_removeInheritance(targetGroup.name, args[2])
                when (result) {
                    PermissionGroup_removeInheritanceResult.SUCCESS -> {
                        sender.sendMessage(mm.deserialize("<green>✔ Removed inheritance of '${args[2]}' from '${targetGroup.name}'.</green>"))
                    }
                    PermissionGroup_removeInheritanceResult.FAILURE_INHERITANCE_NOT_IN_GROUP -> {
                        sender.sendMessage(mm.deserialize("<red>✖ Group '${targetGroup.name}' does not inherit from '${args[2]}'.</red>"))
                    }
                    else -> sender.sendMessage(mm.deserialize("<red>✖ An error occurred while removing inheritance.</red>"))
                }
            }
            else -> {
                sender.sendMessage(mm.deserialize("<red>✖ Unknown subcommand. Use settag, setcolor, addperm, removeperm, addinh, removeinh, or info.</red>"))
            }
        }

        return true
    }

    private fun sendHelpMenu(sender: CommandSender) {
        sender.sendMessage(mm.deserialize("<gray>--------------------------------------------------</gray>"))
        sender.sendMessage(mm.deserialize("<color:#ffcc00><b>★ SurvivalCore Permissions Manager — Groups ★</b></color>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/groups list</color> <gray>»</gray> <white>List all loaded groups.</white>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/groups create <name></color> <gray>»</gray> <white>Create a new group.</white>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/groups delete <name></color> <gray>»</gray> <white>Delete an existing group.</white>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/groups rename <old> <new></color> <gray>»</gray> <white>Rename a group.</white>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/groups <group> info [section]</color> <gray>»</gray> <white>View specific group details.</white>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/groups <group> settag <tag></color> <gray>»</gray> <white>Change a group's chat prefix.</white>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/groups <group> setcolor <color></color> <gray>»</gray> <white>Change a group's primary color.</white>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/groups <group> addperm <perm></color> <gray>»</gray> <white>Add a permission node.</white>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/groups <group> removeperm <perm></color> <gray>»</gray> <white>Remove a permission node.</white>"))
        sender.sendMessage(mm.deserialize(" <color:#00ffcc>/groups <group> addinh/removeinh <group></color> <gray>»</gray> <white>Manage inheritance.</white>"))
        sender.sendMessage(mm.deserialize("<gray>--------------------------------------------------</gray>"))
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
        if (!sender.hasPermission("survivalcore.admin.permissions") && !sender.isOp && sender !is ConsoleCommandSender) {
            return emptyList()
        }

        val loadedGroups = permsAPI.getGroups().map { it.name }
        
        if (args.size == 1) {
            val suggestions = listOf("list", "create", "delete", "rename", "help", "info") + loadedGroups
            return suggestions.filter { it.startsWith(args[0], ignoreCase = true) }
        }

        if (args.size == 2) {
            when (args[0].lowercase()) {
                "delete", "rename" -> {
                    return loadedGroups.filter { it.startsWith(args[1], ignoreCase = true) }
                }
                "info" -> {
                    return loadedGroups.filter { it.startsWith(args[1], ignoreCase = true) }
                }
                "create" -> {
                    return listOf("<groupName>").filter { it.startsWith(args[1], ignoreCase = true) }
                }
            }
            
            // If the first argument was an existing group name
            if (permsAPI.getGroup(args[0]) != null) {
                val subcommands = listOf("settag", "setcolor", "addperm", "removeperm", "addinh", "removeinh", "info")
                return subcommands.filter { it.startsWith(args[1], ignoreCase = true) }
            }
        }

        if (args.size == 3) {
            if (args[0].lowercase() == "info" && permsAPI.getGroup(args[1]) != null) {
                val sections = listOf("inheritances", "perms", "onlineMembers", "members")
                return sections.filter { it.startsWith(args[2], ignoreCase = true) }
            }
            
            val group = permsAPI.getGroup(args[0])
            if (group != null) {
                when (args[1].lowercase()) {
                    "info" -> {
                        val sections = listOf("inheritances", "perms", "onlineMembers", "members")
                        return sections.filter { it.startsWith(args[2], ignoreCase = true) }
                    }
                    "removeperm" -> {
                        return group.perms.filter { it.startsWith(args[2], ignoreCase = true) }
                    }
                    "addinh" -> {
                        return loadedGroups.filter { it != group.name && it.startsWith(args[2], ignoreCase = true) }
                    }
                    "removeinh" -> {
                        val inhNames = group.inheritances.mapNotNull { permsAPI.getGroup(it)?.name }
                        return inhNames.filter { it.startsWith(args[2], ignoreCase = true) }
                    }
                    "setcolor" -> {
                        val colors = NamedTextColor.NAMES.keys().toList()
                        return colors.filter { it.startsWith(args[2], ignoreCase = true) }
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

        if (args.size == 4) {
            val group = permsAPI.getGroup(args[0])
            if (group != null && args[1].lowercase() == "setcolor") {
                val colors = NamedTextColor.NAMES.keys().toList()
                return colors.filter { it.startsWith(args[3], ignoreCase = true) }
            }
        }

        return emptyList()
    }
}
package site.ftka.survivalcore.apps.PermissionsManager.commands

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.PermissionsManager.PermissionsManagerApp
import site.ftka.survivalcore.apps.PermissionsManager.screens.PermissionsManager_ChatScreen
import site.ftka.survivalcore.essentials.chat.ChatAPI
import site.ftka.survivalcore.services.permissions.PermissionsAPI
import site.ftka.survivalcore.services.permissions.subservices.PermissionsService_PlayersSubservice.*
import java.util.*

internal class PermissionsManagerApp_PermsCommand(private val app: PermissionsManagerApp, private val plugin: MClass): CommandExecutor {

    private val chatAPI: ChatAPI
        get() { return plugin.essentialsFwk.chat.api }

    private val permsAPI: PermissionsAPI
        get() { return plugin.servicesFwk.permissions.api }

    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default)

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {

        // player zone
        if (sender !is Player) return false
        val player = sender

        // permission check
        if (!player.hasPermission("survivalcore.admin.permissions")) {
            player.sendMessage(net.kyori.adventure.text.Component.text("You do not have permission to manage permissions.", net.kyori.adventure.text.format.NamedTextColor.RED))
            return true
        }

        // no args presented
        val chatScreen = chatScreen(sender.uniqueId)

        // no args
        if (args.isEmpty()) {
            chatScreen.currentPage = "home"
            chatAPI.showOrRefreshScreen(sender.uniqueId, chatScreen, "")
            return false
        }

        // /perms {player}
        if (args.size == 1) {
            val targetUUID = plugin.essentialsFwk.usernameTracker.getUUID(args[0])
            if (targetUUID == null) {
                player.sendMessage(net.kyori.adventure.text.Component.text("Player '${args[0]}' has never played on this server.", net.kyori.adventure.text.format.NamedTextColor.RED))
                return true
            }

            chatScreen.initializeTarget(false, targetUUID)
            chatAPI.showOrRefreshScreen(player.uniqueId, chatScreen, "player_groups")
            return true
        }


        // /perms {player} addgroup {groupname}
        if (args.size == 3 && args[1].equals("addgroup", true)) {
            val targetUUID = plugin.essentialsFwk.usernameTracker.getUUID(args[0])
            if (targetUUID == null) {
                player.sendMessage(net.kyori.adventure.text.Component.text("Player '${args[0]}' has never played on this server.", net.kyori.adventure.text.format.NamedTextColor.RED))
                return true
            }

            scope.launch {
                val result = permsAPI.player_addGroup(targetUUID, args[2])

                when (result) {
                    Permissions_addGroupResult.SUCCESS -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Successfully added player '${args[0]}' to group '${args[2]}'.", net.kyori.adventure.text.format.NamedTextColor.GREEN))
                    }
                    Permissions_addGroupResult.FAILURE_CORRUPT_PLAYERDATA -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: Player data is corrupted.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                    Permissions_addGroupResult.FAILURE_PLAYER_UNAVAILABLE -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: Player is unavailable.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                    Permissions_addGroupResult.FAILURE_PLAYER_ALREADY_IN_GROUP -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: Player '${args[0]}' is already in group '${args[2]}'.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                    Permissions_addGroupResult.FAILURE_GROUP_UNAVAILABLE -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: Group '${args[2]}' does not exist.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                    Permissions_addGroupResult.FAILURE_UNKNOWN -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: An unknown error occurred.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                }
            }

            return true
        }


        // /perms {player} removegroup {groupname}
        if (args.size == 3 && args[1].equals("removegroup", true)) {
            val targetUUID = plugin.essentialsFwk.usernameTracker.getUUID(args[0])
            if (targetUUID == null) {
                player.sendMessage(net.kyori.adventure.text.Component.text("Player '${args[0]}' has never played on this server.", net.kyori.adventure.text.format.NamedTextColor.RED))
                return true
            }

            scope.launch {
                val result = permsAPI.player_removeGroup(targetUUID, args[2])

                when (result) {
                    Permissions_removeGroupResult.SUCCESS -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Successfully removed player '${args[0]}' from group '${args[2]}'.", net.kyori.adventure.text.format.NamedTextColor.GREEN))
                    }
                    Permissions_removeGroupResult.FAILURE_CORRUPT_PLAYERDATA -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: Player data is corrupted.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                    Permissions_removeGroupResult.FAILURE_PLAYER_UNAVAILABLE -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: Player is unavailable.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                    Permissions_removeGroupResult.FAILURE_PLAYER_NOT_IN_GROUP -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: Player '${args[0]}' is not in group '${args[2]}'.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                    Permissions_removeGroupResult.FAILURE_GROUP_UNAVAILABLE -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: Group '${args[2]}' does not exist.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                    Permissions_removeGroupResult.FAILURE_UNKNOWN -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: An unknown error occurred.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                }
            }

            return true
        }


        // /perms {player} addperm {permission}
        if (args.size == 3 && args[1].equals("addperm", true)) {
            val targetUUID = plugin.essentialsFwk.usernameTracker.getUUID(args[0])
            if (targetUUID == null) {
                player.sendMessage(net.kyori.adventure.text.Component.text("Player '${args[0]}' has never played on this server.", net.kyori.adventure.text.format.NamedTextColor.RED))
                return true
            }

            scope.launch {
                val result = permsAPI.player_addPerm(targetUUID, args[2])

                when (result) {
                    Permissions_addPermissionResult.SUCCESS -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Successfully added permission '${args[2]}' to player '${args[0]}'.", net.kyori.adventure.text.format.NamedTextColor.GREEN))
                    }
                    Permissions_addPermissionResult.FAILURE_CORRUPT_PLAYERDATA -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: Player data is corrupted.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                    Permissions_addPermissionResult.FAILURE_PLAYER_UNAVAILABLE -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: Player is unavailable.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                    Permissions_addPermissionResult.FAILURE_PLAYER_ALREADY_HAS_PERMISSION -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: Player '${args[0]}' already has permission '${args[2]}'.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                    Permissions_addPermissionResult.FAILURE_UNKNOWN -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: An unknown error occurred.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                }
            }

            return true
        }


        // /perms {player} removeperm {permission}
        if (args.size == 3 && args[1].equals("removeperm", true)) {
            val targetUUID = plugin.essentialsFwk.usernameTracker.getUUID(args[0])
            if (targetUUID == null) {
                player.sendMessage(net.kyori.adventure.text.Component.text("Player '${args[0]}' has never played on this server.", net.kyori.adventure.text.format.NamedTextColor.RED))
                return true
            }

            scope.launch {
                val result = permsAPI.player_removePerm(targetUUID, args[2])

                when (result) {
                    Permissions_removePermissionResult.SUCCESS -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Successfully removed permission '${args[2]}' from player '${args[0]}'.", net.kyori.adventure.text.format.NamedTextColor.GREEN))
                    }
                    Permissions_removePermissionResult.FAILURE_CORRUPT_PLAYERDATA -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: Player data is corrupted.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                    Permissions_removePermissionResult.FAILURE_PLAYER_UNAVAILABLE -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: Player is unavailable.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                    Permissions_removePermissionResult.FAILURE_PLAYER_DOESNT_HAVE_PERMISSION -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: Player '${args[0]}' does not have permission '${args[2]}'.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                    Permissions_removePermissionResult.FAILURE_UNKNOWN -> {
                        player.sendMessage(net.kyori.adventure.text.Component.text("Failed: An unknown error occurred.", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                }
            }

            return true
        }

        return true
    }

    private fun chatScreen(uuid: UUID, targettingGroup: Boolean? = null, targetUUID: UUID? = null): PermissionsManager_ChatScreen {
        return if (chatAPI.getActiveScreen(uuid) is PermissionsManager_ChatScreen)
            chatAPI.getActiveScreen(uuid) as PermissionsManager_ChatScreen
        else
            PermissionsManager_ChatScreen(plugin, targettingGroup, targetUUID)
    }

}
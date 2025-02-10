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

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {

        // player zone
        if (sender !is Player) return false
        val player = sender

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
            val targetPlayer = plugin.server.getPlayer(args[0]) ?: return false // send error message

            chatScreen.initializeTarget(false, targetPlayer.uniqueId)
            chatAPI.showOrRefreshScreen(targetPlayer.uniqueId, chatScreen, "player_groups")
            return false
        }


        // /perms {player} addgroup {groupname}
        if (args.size == 3 && args[1].equals("addgroup", true)) {
            val targetPlayer = plugin.server.getPlayer(args[0]) ?: return false // send error message

            GlobalScope.launch {
                val result = permsAPI.player_addGroup(targetPlayer.uniqueId, args[2])

                when (result) { // todo: add messages or screen notifications
                    Permissions_addGroupResult.SUCCESS -> {
                        return@launch
                    }
                    Permissions_addGroupResult.FAILURE_CORRUPT_PLAYERDATA -> {
                        return@launch
                    }
                    Permissions_addGroupResult.FAILURE_PLAYER_UNAVAILABLE -> {
                        return@launch
                    }
                    Permissions_addGroupResult.FAILURE_PLAYER_ALREADY_IN_GROUP -> {
                        return@launch
                    }
                    Permissions_addGroupResult.FAILURE_GROUP_UNAVAILABLE -> {
                        return@launch
                    }
                    Permissions_addGroupResult.FAILURE_UNKNOWN -> {
                        return@launch
                    }
                }
            }

            return true
        }


        // /perms {player} removegroup {groupname}
        if (args.size == 3 && args[1].equals("removegroup", true)) {
            val targetPlayer = plugin.server.getPlayer(args[0]) ?: return false // send error message

            GlobalScope.launch {
                val result = permsAPI.player_removeGroup(targetPlayer.uniqueId, args[2])

                when (result) { // todo: add messages or screen notifications
                    Permissions_removeGroupResult.SUCCESS -> {
                        return@launch
                    }
                    Permissions_removeGroupResult.FAILURE_CORRUPT_PLAYERDATA -> {
                        return@launch
                    }
                    Permissions_removeGroupResult.FAILURE_PLAYER_UNAVAILABLE -> {
                        return@launch
                    }
                    Permissions_removeGroupResult.FAILURE_PLAYER_NOT_IN_GROUP -> {
                        return@launch
                    }
                    Permissions_removeGroupResult.FAILURE_GROUP_UNAVAILABLE -> {
                        return@launch
                    }
                    Permissions_removeGroupResult.FAILURE_UNKNOWN -> {
                        return@launch
                    }
                }
            }

            return true
        }


        // /perms {player} addperm {permission}
        if (args.size == 3 && args[1].equals("addperm", true)) {
            val targetPlayer = plugin.server.getPlayer(args[0]) ?: return false // send error message

            GlobalScope.launch {
                val result = permsAPI.player_addPerm(targetPlayer.uniqueId, args[2])

                when (result) { // todo: add messages or screen notifications
                    Permissions_addPermissionResult.SUCCESS -> {
                        return@launch
                    }
                    Permissions_addPermissionResult.FAILURE_CORRUPT_PLAYERDATA -> {
                        return@launch
                    }
                    Permissions_addPermissionResult.FAILURE_PLAYER_UNAVAILABLE -> {
                        return@launch
                    }
                    Permissions_addPermissionResult.FAILURE_PLAYER_ALREADY_HAS_PERMISSION -> {
                        return@launch
                    }
                    Permissions_addPermissionResult.FAILURE_UNKNOWN -> {
                        return@launch
                    }
                }
            }

            return true
        }


        // /perms {player} removeperm {permission}
        if (args.size == 3 && args[1].equals("removeperm", true)) {
            val targetPlayer = plugin.server.getPlayer(args[0]) ?: return false // send error message

            GlobalScope.launch {
                val result = permsAPI.player_removePerm(targetPlayer.uniqueId, args[2])

                when (result) { // todo: add messages or screen notifications
                    Permissions_removePermissionResult.SUCCESS -> {
                        return@launch
                    }
                    Permissions_removePermissionResult.FAILURE_CORRUPT_PLAYERDATA -> {
                        return@launch
                    }
                    Permissions_removePermissionResult.FAILURE_PLAYER_UNAVAILABLE -> {
                        return@launch
                    }
                    Permissions_removePermissionResult.FAILURE_PLAYER_DOESNT_HAVE_PERMISSION -> {
                        return@launch
                    }
                    Permissions_removePermissionResult.FAILURE_UNKNOWN -> {
                        return@launch
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
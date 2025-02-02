package site.ftka.survivalcore.apps.PermissionsManager.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.PermissionsManager.PermissionsManagerApp
import site.ftka.survivalcore.apps.PermissionsManager.screens.PermissionsManager_ChatScreen
import site.ftka.survivalcore.services.chat.ChatAPI
import site.ftka.survivalcore.services.permissions.PermissionsAPI
import site.ftka.survivalcore.services.permissions.subservices.PermissionsService_GroupsSubservice.*
import java.util.*

internal class PermissionsManagerApp_GroupsCommand(private val app: PermissionsManagerApp, private val plugin: MClass): CommandExecutor {

    private val chatAPI: ChatAPI
        get() { return plugin.servicesFwk.chat.api }

    private val permsAPI: PermissionsAPI
        get() { return plugin.servicesFwk.permissions.api }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {

        // player zone
        if (sender !is Player) return false
        val player = sender

        // no args presented
        val chatScreen = chatScreen(sender.uniqueId)


        // no args
        if (args.isEmpty()) {
            chatScreen.currentPage = "groups_home"
            chatAPI.showOrRefreshScreen(sender.uniqueId, chatScreen, "")
            return false
        }


        // /group {name}
        if (args.size == 1) {
            val targetUUID = permsAPI.getGroup(args[0]) ?: return false // send error message

            chatScreen.initializeTarget(false, targetUUID.uuid)
            chatAPI.showOrRefreshScreen(player.uniqueId, chatScreen, "group_perms")
            return true
        }


        // /groups {name} addperm {perm}
        if (args.size == 3 && args[1].equals("addperm", true)) {
            val result = permsAPI.group_addPerm(args[0], args[2])

            when (result) { // todo: add messages or screen notifications
                PermissionGroup_addPermissionResult.SUCCESS -> {
                    return true
                }
                PermissionGroup_addPermissionResult.FAILURE_GROUP_DOES_NOT_EXIST -> {
                    return false
                }
                PermissionGroup_addPermissionResult.FAILURE_PERMISSION_ALREADY_EXISTS -> {
                    return false
                }
                PermissionGroup_addPermissionResult.FAILURE_UNKNOWN -> {
                    return false
                }
            }
        }


        // /groups {name} removeperm {perm}
        if (args.size == 3 && args[1].equals("removeperm", true)) {
            val result = permsAPI.group_removePerm(args[0], args[2])

            when (result) { // todo: add messages or screen notifications
                PermissionGroup_removePermissionResult.SUCCESS -> {
                    return true
                }
                PermissionGroup_removePermissionResult.FAILURE_GROUP_DOES_NOT_EXIST -> {
                    return false
                }
                PermissionGroup_removePermissionResult.FAILURE_PERMISSION_DOES_NOT_EXIST -> {
                    return false
                }
                PermissionGroup_removePermissionResult.FAILURE_UNKNOWN -> {
                    return false
                }
            }
        }


        // /groups {name} addinheritance/addinh {group}
        if (args.size == 3 && (args[1].equals("addinheritance", true) || args[1].equals("addinh", true))) {
            val result = permsAPI.group_addInheritance(args[0], args[2])

            when (result) { // todo: add messages or screen notifications
                PermissionGroup_addInheritanceResult.SUCCESS -> {
                    return true
                }
                PermissionGroup_addInheritanceResult.FAILURE_GROUP_DOES_NOT_EXIST -> {
                    return false
                }
                PermissionGroup_addInheritanceResult.FAILURE_INHERITANCE_GROUP_DOES_NOT_EXIST -> {
                    return false
                }
                PermissionGroup_addInheritanceResult.FAILURE_INHERITANCE_ALREADY_SET -> {
                    return false
                }
                PermissionGroup_addInheritanceResult.FAILURE_UNKNOWN -> {
                    return false
                }
            }
        }


        // /groups {name} removeinheritance/removeinh {group}
        if (args.size == 3 && (args[1].equals("removeinheritance", true) || args[1].equals("removeinh", true))) {
            val result = permsAPI.group_removeInheritance(args[0], args[2])

            when (result) { // todo: add messages or screen notifications
                PermissionGroup_removeInheritanceResult.SUCCESS -> {
                    return true
                }
                PermissionGroup_removeInheritanceResult.FAILURE_GROUP_DOES_NOT_EXIST -> {
                    return false
                }
                PermissionGroup_removeInheritanceResult.FAILURE_INHERITANCE_NOT_IN_GROUP-> {
                    return false
                }
                PermissionGroup_removeInheritanceResult.FAILURE_INHERITANCE_GROUP_DOES_NOT_EXIST -> {
                    return false
                }
                PermissionGroup_removeInheritanceResult.FAILURE_UNKNOWN -> {
                    return false
                }
            }
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
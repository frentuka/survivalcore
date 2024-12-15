package site.ftka.survivalcore.apps.PermissionsManager.commands

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.PermissionsManager.PermissionsManagerApp
import site.ftka.survivalcore.services.chat.objects.ChatScreen

class PermissionsManagerApp_Command(private val app: PermissionsManagerApp, private val plugin: MClass): CommandExecutor {

    private enum class CommandType {
        GROUP,
        PLAYER
    }

    private enum class CommandAction {
        LIST,
        ADD,
        REMOVE
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>?): Boolean {
        if (args == null || args.isEmpty()) {
            sender.sendMessage("Permissions Manager")
            return false
        }

        if (sender is Player)
            if (!plugin.servicesFwk.permissions.api.playerHasPerm(sender.uniqueId, "staff.permissions.manage")) {
                sender.sendMessage("NO")
                return false
            }

        // 1st arg: command type (group/player)

        val cmdType = when (args[0]) {
            "group" -> CommandType.GROUP
            "player" -> CommandType.PLAYER
            else -> {
                sender.sendMessage("Invalid command type")
                return false
            }
        }

        // second arg: <group name/player name>

        if (cmdType == CommandType.GROUP) {
            // group
            if (args.size < 2) {
                sender.sendMessage("Invalid group name")
                return false
            }

            if (plugin.servicesFwk.permissions.getGroup(args[1]) == null) {
                sender.sendMessage("Group not found")
                return false
            }
        } else {
            // player
            if (args.size < 2) {
                sender.sendMessage("Invalid player name")
                return false
            }

            if (plugin.essentialsFwk.usernameTracker.getUUID(args[1]) == null) {
                sender.sendMessage("Player not found")
                return false
            }
        }

        // third arg: <list/add/remove>

        val action: CommandAction = when (args[2]) {
            "list" -> CommandAction.LIST
            "add" -> CommandAction.ADD
            "remove" -> CommandAction.REMOVE
            else -> {
                sender.sendMessage("Invalid action")
                return false
            }
        }

        // fourth arg: <permission> (if add/remove)

        if (action == CommandAction.ADD || action == CommandAction.REMOVE) {
            if (args.size < 4) {
                sender.sendMessage("Invalid permission")
                return false
            }
        }

        // execute command

        when (cmdType) {
            CommandType.GROUP -> {
                val group = plugin.servicesFwk.permissions.getGroup(args[1])!!
                when (action) {
                    CommandAction.LIST -> {
                        val msg = StringBuilder()
                        msg.append("Group: ${group.name}")
                        msg.append("Permissions:")
                        group.perms.forEach { msg.append("\n$it") }
                        sender.sendMessage(msg.toString())
                    }
                    CommandAction.ADD -> {
                        // plugin.servicesFwk.permissions.pla NOT DONE
                        plugin.servicesFwk.permissions.materializeGroup(group)
                        sender.sendMessage("Permission added")
                    }
                    CommandAction.REMOVE -> {
                        // group.perms.remove(args[3]) NOT DONE
                        plugin.servicesFwk.permissions.materializeGroup(group)
                        sender.sendMessage("Permission removed")
                    }
                }
            }
            CommandType.PLAYER -> {
                val uuid = plugin.essentialsFwk.usernameTracker.getUUID(args[1])!!
                when (action) {
                    CommandAction.LIST -> {
                        val msg = StringBuilder()
                        msg.append("Player: ${args[1]}")
                        msg.append("Permissions:")
                        // plugin.servicesFwk.permissions.players_ss.get NOT DONE
                        // screen.send(sender)
                    }
                    CommandAction.ADD -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            plugin.servicesFwk.permissions.players_ss.addPermissionToPlayer(plugin.essentialsFwk.usernameTracker.getUUID(args[1])!!, args[3])
                            sender.sendMessage("Permission added")
                        }
                    }
                    CommandAction.REMOVE -> {
                        //player.permissions?.permissions?.remove(args[3]) NOT DONE
                        //plugin.servicesFwk.playerData.inout_ss.save(player)
                        sender.sendMessage("Permission removed")
                    }
                }
            }
        }

        return true
    }




}
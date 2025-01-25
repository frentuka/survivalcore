package site.ftka.survivalcore.apps.PermissionsManager.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.PermissionsManager.PermissionsManagerApp
import site.ftka.survivalcore.apps.PermissionsManager.lang.PermissionsManager_CommandLang
import site.ftka.survivalcore.services.chat.objects.ChatScreen
import java.util.UUID

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

    private val chatScreen = ChatScreen("PermissionsManager")
        get() {
            field.screenContent["main"] = PermissionsManager_CommandLang(plugin).screenPage_home_panel()
            field.screenContent["group"] = PermissionsManager_CommandLang(plugin).screenPage_group_panel()
            return field
        }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        // no args presented
        if (args.isEmpty() && sender is Player) {
            sendCommandScreen(sender.uniqueId, "main")
            return false
        }

        // args presented

        // group
        if ((args.size == 1) and args[0].equals("group", true) and (sender is Player)) {
            sendCommandScreen((sender as Player).uniqueId, "group")
            return true
        }

        return true
    }


    private fun sendCommandScreen(sender: UUID, page: String) {
        chatScreen.currentPage = page
        plugin.servicesFwk.chat.screens_ss.showScreen(sender, chatScreen)
    }


}
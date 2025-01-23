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

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty() && sender is Player) {
            sendCommandScreen(sender.uniqueId)
            return false
        }

        return true
    }


    private fun sendCommandScreen(sender: UUID) {
        val screen = ChatScreen("perms")
        screen.screenContent["main"] = PermissionsManager_CommandLang().ScreenPage_home_panel()
        screen.currentPage = "main"

        plugin.servicesFwk.chat.screens_ss.showScreen(sender, screen)
    }


}
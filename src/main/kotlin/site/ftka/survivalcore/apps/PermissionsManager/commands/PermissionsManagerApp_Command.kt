package site.ftka.survivalcore.apps.PermissionsManager.commands

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
//        if (args == null || args.isEmpty()) {
//            sender.sendMessage("Permissions Manager")
//            return false
//        }

        // test: make player housemaster
        if (sender is Player)
            sender.sendMessage(plugin.servicesFwk.permissions.players_ss.addGroupToPlayer(sender.uniqueId, "housemaster").toString())





        var cmdType: CommandType? = null

//        if (args.size == 0) {
//            // show home panel
//            val screen = ChatScreen("permissionsManagerHome_${sender.uniqueId}", app.lang.get("permissionsManager.home_panel"))
//            return false
//        }

//        // first arg: <group/player>
//        if (args[0] == "group") {
//            cmdType = CommandType.GROUP
//            return false
//        } else if (args[0] == "player") {
//            cmdType = CommandType.PLAYER
//            return false
//        }

        // second arg: <group name/player name>

        // third arg: <list/add/remove>

        // fourth arg: <permission> (if add/remove)

        return true
    }


}
package site.ftka.survivalcore.apps.ServerAdministration.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass

class ServerAdministration_serverCommand(private val plugin: MClass): CommandExecutor {

    // shortcut vars
    private val essFwk = plugin.essentialsFwk
    private val servFwk = plugin.servicesFwk

    private val SERVER_ADMINISTRATION_PLAYER_PERMISSION = "staff.admin"

    private enum class ServicesEnum() {
        CHAT, INVENTORYGUI, LANGUAGE, PERMISSIONS, PLAYERDATA
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is ConsoleCommandSender) {
            if (sender !is Player) return false
            else if (!servFwk.playerDataService.playerDataMap.containsKey(sender.uniqueId)) return false // if not exists in pdata
            if (!servFwk.permissionsService.permissions_ss.playerHasPerm(sender.uniqueId, SERVER_ADMINISTRATION_PLAYER_PERMISSION))
                return false // if player does not have permission
        }

        if (args?.size == 0) return false // todo: send full command usage

        return false
    }

}
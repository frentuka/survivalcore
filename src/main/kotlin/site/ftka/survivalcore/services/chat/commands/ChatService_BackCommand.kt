package site.ftka.survivalcore.services.chat.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import site.ftka.survivalcore.services.chat.ChatService

internal class ChatService_BackCommand(private val svc: ChatService): CommandExecutor {

    private val logger = svc.logger.sub("ExitScreen")

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // /backscreen

        if (sender !is Player) {
            logger.log("Only players can use this command.")
            return false
        }

        if (!svc.api.isPlayerInsideScreen(sender.uniqueId))
            return false

        svc.screens_ss.getActiveScreen(sender.uniqueId)?.previousPage()
        svc.screens_ss.refreshScreen(sender.uniqueId,
            svc.screens_ss.getActiveScreen(sender.uniqueId)!!.name,
            svc.screens_ss.getActiveScreen(sender.uniqueId)!!.currentPage)

        return false
    }

}
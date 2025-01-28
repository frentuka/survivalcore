package site.ftka.survivalcore.services.chat.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import site.ftka.survivalcore.services.chat.ChatService

internal class ChatService_ExitScreenCommand(private val svc: ChatService): CommandExecutor {

    private val logger = svc.logger.sub("ExitScreen")

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // /exitScreen

        if (sender !is Player) {
            logger.log("Only players can use this command.")
            return false
        }

        svc.screens_ss.stopAnyScreen(sender.uniqueId)

        return false
    }

}
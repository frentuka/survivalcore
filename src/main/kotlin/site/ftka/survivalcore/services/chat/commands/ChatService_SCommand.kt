package site.ftka.survivalcore.services.chat.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.TabCompleteEvent
import site.ftka.survivalcore.services.chat.ChatService

class ChatService_SCommand(private val svc: ChatService): CommandExecutor, Listener {

    @EventHandler
    fun onTabComplete(ev: TabCompleteEvent) {
        if (ev.sender !is Player) return
        val uuid = (ev.sender as Player).uniqueId

        if (!svc.screens_ss.isPlayerInsideScreen(uuid)) return
        if (!ev.buffer.startsWith("/s ")) return

        ev.completions = listOf()

        val text = ev.buffer.replace("/s ", "")
        val args = text.split(" ")

        svc.screens_ss.getActiveScreen(uuid)?.let{ screen ->
            screen.getCurrentChatScreenPageObject().let { page ->
                val completions = page?.onSCommandTabComplete?.let { it(args, ev.sender as Player) }
                ev.completions = completions ?: return
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        val uuid = sender.uniqueId

        if (!svc.screens_ss.isPlayerInsideScreen(uuid)) return false
        if (args.isEmpty()) return false

        svc.screens_ss.getActiveScreen(uuid)?.let{ screen ->
            screen.getCurrentChatScreenPageObject().let { page ->
                    page?.onSCommand?.let { it(args.toList(), sender)
                        svc.api.refreshScreen(uuid, screen.name, screen.currentPage)
                }
            } ?: return false
        } ?: return false

        return true
    }

}
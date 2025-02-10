package site.ftka.survivalcore.apps.ChatManager.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.ChatManager.screens.ChatManager_ChatScreen
import java.util.UUID

class ChatManager_ChatCommand(private val plugin: MClass): CommandExecutor {

    private val chatAPI = plugin.essentialsFwk.chat.api

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
//        if (sender is Player)
//            if (!plugin.servicesFwk.permissions.api.playerHasPerm(sender.uniqueId, "staff.*")) {
//                // player does not have permission
//                return false
//            }
        if (sender !is Player)
            return false

        val uuid = sender.uniqueId

        if (plugin.servicesFwk.permissions.api.player_hasPerm_locally(uuid, "staff.chat")) {
            // player does not have permission
            return false
        }

        // screen
        val chatScreen = chatScreen(uuid)

        // commands:

        // /chat
        // -> shows command screen
        if (args.isEmpty()) {
            chatScreen.currentPage = "home"
            chatAPI.showScreen(uuid, chatScreen)
            return false
        }

        // /chat {player}
        if (args.size == 1) {
            val targetPlayer = plugin.server.getPlayer(args[0])
                ?: run {
                    chatScreen.homePanel_chatMessage = args[0]
                    chatScreen.homePanel_isOnline = false
                    chatScreen.homePanel_errorMsgTimeout = 3
                    return false
                }

            chatScreen.playerSelection(targetPlayer.uniqueId)

            chatScreen.homePanel_isOnline = true

            chatAPI.refreshScreen(uuid, chatScreen.name, "player")
            return true
        }

        // /chat {player} channels
        if (args.size == 2 && args[1].equals("channels", true)) {
            if (plugin.server.getPlayer(args[0]) == null) {
                return false
            }

            chatScreen.playerSelection(plugin.server.getPlayer(args[0])!!.uniqueId)
            chatAPI.refreshScreen(uuid, chatScreen.name, "player_channels")
            return false
        }

        // /chat {player} channels disable {channel}
        if (args.size == 4 && args[1].equals("channels", true) && args[2].equals("disable", true)) {
            if (plugin.server.getPlayer(args[0]) == null)
                return false

            chatAPI.removeActiveChannel(uuid, args[3])

            chatScreen.playerSelection(plugin.server.getPlayer(args[0])!!.uniqueId)
            chatAPI.refreshScreen(uuid, chatScreen.name, "player_channels")
            return false
        }

        // /chat {player} screen
        // -> shows if player is inside a screen. if so, allows to stop screen
        if (args.size == 2 && args[1].equals("screen", true)) {
            if (plugin.server.getPlayer(args[0]) == null)
                return false

            chatScreen.playerSelection(plugin.server.getPlayer(args[0])!!.uniqueId)
            chatAPI.refreshScreen(uuid, chatScreen.name, "player_screen")

            return false
        }

        // /chat {playerName} screen stop
        if (args.size == 3 && args[1].equals("screen", true) && args[2].equals("stop", true)) {
            if (plugin.server.getPlayer(args[0]) == null)
                return false

            chatAPI.stopAnyScreen(plugin.server.getPlayer(args[0])!!.uniqueId)
            return false
        }

        return false
    }

    private fun chatScreen(uuid: UUID, targetUUID: UUID? = null): ChatManager_ChatScreen {
        return if (chatAPI.getActiveScreen(uuid) is ChatManager_ChatScreen)
            chatAPI.getActiveScreen(uuid) as ChatManager_ChatScreen
        else
            ChatManager_ChatScreen(plugin, targetUUID)
    }

}
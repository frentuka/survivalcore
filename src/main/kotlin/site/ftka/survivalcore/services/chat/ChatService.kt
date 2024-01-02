package site.ftka.survivalcore.services.chat

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chat.objects.ChatChannel
import site.ftka.survivalcore.services.chat.objects.ChatData
import java.util.UUID

class ChatService(var plugin: MClass) {

    /*
        ChatService

        This service is meant to fully control
        every player's chat.
     */

    private val playerChatMap = mutableMapOf<UUID, ChatData>()
    private val chatChannelMap = mutableMapOf<String, ChatChannel>()
    private val globalChatMap = ChatData(mutableMapOf(),60)

    fun init() {

    }

    fun restart() {

    }

    fun sendMessageToPlayer(uuid: UUID, message: Component) {
        // get online player
        val player = plugin.server.getPlayer(uuid) ?: return

        // send the message
        player.sendMessage(message)
    }

}

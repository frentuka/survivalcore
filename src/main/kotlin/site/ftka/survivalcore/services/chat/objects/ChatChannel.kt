package site.ftka.survivalcore.services.chat.objects

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.services.chat.ChatService
import java.util.UUID

data class ChatChannel(private val service: ChatService, val name: String, var messageTimeoutSeconds: Int = 1800) {
    val members = mutableSetOf<UUID>()
    val chatdata = ChatData()

    fun addMember(uuid: UUID) = members.add(uuid)
    fun removeMember(uuid: UUID) = members.remove(uuid)

    fun sendMessage(message: Component) {
        chatdata.add(System.currentTimeMillis(), message)
        for (member in members)
            service.sendMessageToPlayer(member, message)
    }
}
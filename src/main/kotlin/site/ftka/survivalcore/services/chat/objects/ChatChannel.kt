package site.ftka.survivalcore.services.chat.objects

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.services.chat.ChatService
import java.util.UUID

data class ChatChannel(private val service: ChatService, val name: String, var settings: ChatChannelSettings = ChatChannelSettings()) {
    val members = mutableSetOf<UUID>()
    val data = ChatData()

    fun addMember(uuid: UUID) = members.add(uuid)
    fun removeMember(uuid: UUID) = members.remove(uuid)

    private fun store(message: Component) {
        data.add(System.currentTimeMillis(), message)

        // prevent message data from exceeding limit
        if (settings.maxChatEntries < 0) return // no limit if max is < 0
        while (data.chatMap.keys.size > settings.maxChatEntries)
            data.chatMap.remove(data.chatMap.keys.min()) // removes the older value
    }

    /*
            PUBLIC ZONE
     */

    fun sendMessage(message: Component, storeMessageData: Boolean = true) {
        if (storeMessageData) store(message)

        for (member in members)
            service.sendRawMessageToPlayer(member, message)
    }

    /**
     *  Shortcut to data.getLatest
     */
    fun getLatestMessages(amount: Int) = data.getLatest(amount)
}
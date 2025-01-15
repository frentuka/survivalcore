package site.ftka.survivalcore.services.chat.objects

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.services.chat.ChatService
import java.util.UUID

data class ChatChannel(private val service: ChatService, val name: String, var settings: ChatChannelSettings = ChatChannelSettings()) {
    val members = mutableSetOf<UUID>()
    val data = ChatData()

    var lastMessage = System.currentTimeMillis()

    class ChatChannelSettings {
        var maxStoredChatEntries: Int = 25
        var timeoutAfterSeconds: Int = 7200 // 2 hours
    }

    fun addMember(uuid: UUID) = members.add(uuid)
    fun removeMember(uuid: UUID) = members.remove(uuid)

    /**
     * Message won't be sent
     */
    fun addMessage(message: Component) {
        var timeMillis = System.currentTimeMillis()

        // another message could have the exact same timeMillis
        while (data.chatMap.containsKey(timeMillis))
            timeMillis+= 1

        data.add(message, timeMillis)
        lastMessage = timeMillis

        // prevent message data from exceeding limit
        if (settings.maxStoredChatEntries < 0) return // no limit if max is < 0
        while (data.chatMap.keys.size > settings.maxStoredChatEntries)
            data.chatMap.remove(data.chatMap.keys.min()) // removes the older value
    }

    /**
     *  Shortcut to data.getLatest
     */
    fun getLatestMessages(amount: Int) =
        data.getLatest(amount)
}
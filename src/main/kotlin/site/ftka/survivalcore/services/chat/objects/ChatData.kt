package site.ftka.survivalcore.services.chat.objects

import net.kyori.adventure.text.Component

data class ChatData(private var chatMap: MutableMap<Long, Component> = mutableMapOf(), private val MAX_CHAT_ENTRIES: Int = 25) {

    fun add(timestamp: Long, data: Component) {
        chatMap[timestamp] = data
        removeExcess()
    }

    fun getLatest(amount: Int): List<Component> {
        return chatMap
            .entries
            .sortedByDescending { it.key }
            .take(amount)
            .map { it.value }
    }

    private fun removeExcess() {
        while (chatMap.size > MAX_CHAT_ENTRIES)
            chatMap.remove(chatMap.keys.min())
    }

}
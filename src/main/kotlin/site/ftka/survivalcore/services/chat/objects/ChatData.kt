package site.ftka.survivalcore.services.chat.objects

import net.kyori.adventure.text.Component

data class ChatData(val chatMap: MutableMap<Long, Component> = mutableMapOf()) {

    fun add(timestamp: Long, data: Component) {
        chatMap[timestamp] = data
    }

    fun getLatest(amount: Int): List<Component> {
        return chatMap
            .entries
            .sortedByDescending { it.key }
            .take(amount)
            .map { it.value }
    }

}
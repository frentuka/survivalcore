package site.ftka.survivalcore.services.chat.objects

import net.kyori.adventure.text.Component

data class ChatData(val chatMap: MutableMap<Long, Component> = mutableMapOf()) {

    fun add(data: Component, timestamp: Long = System.currentTimeMillis()) {
        chatMap[timestamp] = data
    }

    // Returns chatMap with only {amount} elements sorted by key from higher
    fun getLatest(amount: Int): Map<Long, Component> =
        chatMap.toSortedMap().entries.take(amount).associate { it.key to it.value }

}
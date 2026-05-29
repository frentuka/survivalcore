package site.ftka.survivalcore.essentials.chat.objects

import net.kyori.adventure.text.Component
import java.util.concurrent.ConcurrentSkipListMap

data class ChatData(val chatMap: MutableMap<Long, Component> = ConcurrentSkipListMap()) {

    fun add(data: Component, timestamp: Long = System.currentTimeMillis()) {
        chatMap[timestamp] = data
    }

    // Returns chatMap with only {amount} elements sorted by key from higher (most recent)
    fun getLatest(amount: Int): Map<Long, Component> {
        val skipListMap = chatMap as? ConcurrentSkipListMap<Long, Component>
        return if (skipListMap != null) {
            skipListMap.descendingMap().entries.take(amount).associate { it.key to it.value }
        } else {
            chatMap.toSortedMap(reverseOrder()).entries.take(amount).associate { it.key to it.value }
        }
    }
}
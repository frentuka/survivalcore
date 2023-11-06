package site.ftka.survivalcore.essentials.database.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class DatabaseHealthCheckFailedEvent(): Event(true) {

    companion object {
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }
    }

    private val handlers = HandlerList()

    override fun getHandlers(): HandlerList {
        return handlers
    }
}
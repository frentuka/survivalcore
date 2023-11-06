package site.ftka.survivalcore.services.playerdata.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerDataInitEvent: Event(false) {

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
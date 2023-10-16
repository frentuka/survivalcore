package site.ftka.survivalcore.services.database.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class DatabaseHealthCheckFailedEvent(): Event(true) {

    private val handlerList = HandlerList()
    override fun getHandlers(): HandlerList = handlerList
    fun getHandlerList() = handlerList
}
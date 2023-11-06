package site.ftka.survivalcore.services.playerdata.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*

class PlayerDataJoinEvent(val uuid: UUID, val playerdata: PlayerData?): Event(false) {

    /*
        JoinEvent but managed after PlayerData is ready
     */

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
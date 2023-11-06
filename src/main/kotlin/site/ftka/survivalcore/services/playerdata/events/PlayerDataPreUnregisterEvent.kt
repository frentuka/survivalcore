package site.ftka.survivalcore.services.playerdata.events

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*

class PlayerDataPreUnregisterEvent(val uuid: UUID, val playerdata: PlayerData, val player: Player): Event(false) {

    /*
        Executed before unregistering PlayerData
        so that modules can refresh their information
        before saving into database.
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
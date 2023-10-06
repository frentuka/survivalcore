package site.ftka.survivalcore.services.playerdata.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.UUID

class PlayerDataRegisterEvent(val uuid: UUID, val playerdata: PlayerData?): Event(true) {

    /*
        Register event of player's data
     */

    private val handlerList = HandlerList()
    override fun getHandlers(): HandlerList = handlerList
}
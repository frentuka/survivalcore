package site.ftka.survivalcore.services.playerdata.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*

class PlayerDataJoinEvent(val uuid: UUID, val playerdata: PlayerData?): Event(true) {

    /*
        JoinEvent but managed after PlayerData is ready
     */

    private val handlerList = HandlerList()
    override fun getHandlers(): HandlerList = handlerList
}
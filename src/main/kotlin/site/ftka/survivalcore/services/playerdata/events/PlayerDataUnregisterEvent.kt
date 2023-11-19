package site.ftka.survivalcore.services.playerdata.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import site.ftka.survivalcore.essentials.proprietaryEvents.objects.PropEvent
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*

class PlayerDataUnregisterEvent(val uuid: UUID, val playerdata: PlayerData?): PropEvent {

    override val async = false
    override var cancelled = false

    /*
        Register event of player's data
     */

}
package site.ftka.survivalcore.services.playerdata.events

import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropEvent
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*

class PlayerDataUnregisterEvent(val uuid: UUID, val playerdata: PlayerData?): PropEvent {
    override val name = "PlayerDataUnregisterEvent"
    override val async = false
    override var cancelled = false

    /*
        Register event of player's data
     */

}
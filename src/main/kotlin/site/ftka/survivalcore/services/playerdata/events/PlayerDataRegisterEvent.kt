package site.ftka.survivalcore.services.playerdata.events

import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropEvent
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*

class PlayerDataRegisterEvent(val uuid: UUID, val playerdata: PlayerData?, val isFirstJoin: Boolean = false):
    PropEvent {
    override val name = "PlayerDataRegisterEvent"
    override val async = false
    override var cancelled = false

    /*
        Register event of player's data
     */

}
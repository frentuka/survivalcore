package site.ftka.survivalcore.services.playerdata.events

import site.ftka.survivalcore.essentials.proprietaryEvents.objects.PropEvent
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*

class PlayerDataRegisterEvent(val uuid: UUID, val playerdata: PlayerData?): PropEvent {

    override val async = false
    override var cancelled = false

    /*
        Register event of player's data
     */

}
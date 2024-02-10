package site.ftka.survivalcore.services.playerdata.events

import site.ftka.survivalcore.initless.proprietaryEvents.objects.PropEvent
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*

class PlayerDataJoinEvent(val uuid: UUID, val playerdata: PlayerData?): PropEvent {
    override val name = "PlayerDataJoinEvent"
    override val async = false
    override var cancelled = false

    /*
        JoinEvent but managed after PlayerData is ready
     */

}
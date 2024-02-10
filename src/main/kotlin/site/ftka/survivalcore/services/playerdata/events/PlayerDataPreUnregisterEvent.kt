package site.ftka.survivalcore.services.playerdata.events

import org.bukkit.entity.Player
import site.ftka.survivalcore.initless.proprietaryEvents.objects.PropEvent
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*

class PlayerDataPreUnregisterEvent(val uuid: UUID, val playerdata: PlayerData, val player: Player): PropEvent {
    override val name = "PlayerDataPreUnregisterEvent"
    override val async = false
    override var cancelled = false

    /*
        Executed before unregistering PlayerData
        so that modules can refresh their information
        before saving into database.
     */

}
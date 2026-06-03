package site.ftka.survivalcore.services.chunkborder.events

import org.bukkit.block.Block
import org.bukkit.entity.Player
import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropEvent

class BorderPunchEvent(val player: Player, val clickedBlock: Block) : PropEvent {
    override val name: String = "BorderPunchEvent"
    override val async: Boolean = false
    override var cancelled: Boolean = false
}

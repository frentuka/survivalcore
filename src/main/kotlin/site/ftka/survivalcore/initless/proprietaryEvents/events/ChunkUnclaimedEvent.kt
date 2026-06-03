package site.ftka.survivalcore.initless.proprietaryEvents.events

import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropEvent
import java.util.UUID

class ChunkUnclaimedEvent(val playerUuid: UUID, val chunkX: Int, val chunkZ: Int) : PropEvent {
    override val name: String = "ChunkUnclaimedEvent"
    override val async: Boolean = true
    override var cancelled: Boolean = false
}

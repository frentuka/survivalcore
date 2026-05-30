package site.ftka.survivalcore.initless.proprietaryEvents.events

import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropEvent
import java.util.UUID

class ChunkClaimedEvent(val playerUuid: UUID, val chunkX: Int, val chunkZ: Int) : PropEvent {
    override val name: String = "ChunkClaimedEvent"
    override val async: Boolean = true
    override var cancelled: Boolean = false
}

package site.ftka.survivalcore.services.worldboard

import net.kyori.adventure.text.Component
import org.bukkit.Location
import site.ftka.survivalcore.services.worldboard.objects.WorldBoardInstance

class WorldBoardAPI internal constructor(private val svc: WorldBoardService) {

    /**
     * Spawns a new holographic board in the world.
     * @param id A unique identifier for tracking this board globally.
     * @param location The coordinates where the board should float.
     * @param text The initial rich text to display.
     */
    fun createBoard(id: String, location: Location, text: Component, builder: (WorldBoardInstance.() -> Unit)? = null): WorldBoardInstance {
        return svc.createBoard(id, location, text, builder)
    }

    /**
     * Retrieves an active holographic board by its ID.
     */
    fun getBoard(id: String): WorldBoardInstance? {
        return svc.getBoard(id)
    }

    /**
     * Teleports a board to a new location.
     */
    fun teleportBoard(id: String, location: Location, durationTicks: Int = 0) {
        svc.getBoard(id)?.teleport(location, durationTicks)
    }

    /**
     * Safely removes and despawns a board by its ID.
     */
    fun removeBoard(id: String) {
        svc.removeBoard(id)
    }
}

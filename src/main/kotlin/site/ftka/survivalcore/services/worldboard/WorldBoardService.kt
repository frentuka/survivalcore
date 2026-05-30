package site.ftka.survivalcore.services.worldboard

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import site.ftka.survivalcore.services.ServicesFramework
import site.ftka.survivalcore.services.worldboard.objects.WorldBoardInstance
import java.util.concurrent.ConcurrentHashMap

class WorldBoardService(private val plugin: MClass, private val services: ServicesFramework) : Listener {
    internal val logger = plugin.loggingInitless.getLog("WorldBoard", Component.text("WorldBoard").color(TextColor.fromHexString("#00ffcc")))
    val api = WorldBoardAPI(this)

    // Thread-safe registry mapping custom IDs to active board wrappers
    private val activeBoards = ConcurrentHashMap<String, WorldBoardInstance>()

    internal fun init() {
        logger.log("Initializing...", LogLevel.LOW)
        plugin.server.pluginManager.registerEvents(this, plugin)
        
        // Initial scan of already loaded chunks on server start, scheduled safely on Folia chunk threads
        for (world in plugin.server.worlds) {
            for (chunk in world.loadedChunks) {
                plugin.server.regionScheduler.execute(plugin, world, chunk.x, chunk.z) {
                    for (entity in chunk.entities) {
                        if (entity is TextDisplay) {
                            cleanIfOrphan(entity)
                        }
                    }
                }
            }
        }
    }

    internal fun restart() {
        logger.log("Restarting...", LogLevel.LOW)
        stop()
        init()
    }

    internal fun stop() {
        logger.log("Stopping... Clearing all active display entities.", LogLevel.LOW)
        clearAllBoards()
    }

    internal fun createBoard(id: String, location: Location, text: Component, builder: (WorldBoardInstance.() -> Unit)? = null): WorldBoardInstance {
        // If a board with this ID already exists, despawn it first to prevent duplicates
        activeBoards[id]?.remove(instant = true)

        val instance = WorldBoardInstance(plugin, id, location, text)
        if (builder != null) {
            instance.builder()
        }
        activeBoards[id] = instance
        instance.spawn()
        return instance
    }

    internal fun getBoard(id: String): WorldBoardInstance? = activeBoards[id]

    internal fun removeBoard(id: String) {
        activeBoards.remove(id)?.remove()
    }

    private fun clearAllBoards() {
        for (board in activeBoards.values) {
            board.remove(instant = true)
        }
        activeBoards.clear()
    }

    /**
     * Listens to chunk loads and automatically scans for and sweeps away any orphaned worldboards
     * directly on the chunk's region thread.
     */
    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        val chunk = event.chunk
        for (entity in chunk.entities) {
            if (entity is TextDisplay) {
                cleanIfOrphan(entity)
            }
        }
    }

    /**
     * Checks if a TextDisplay is an orphaned WorldBoard or a legacy ghost from before the tracking tag was added,
     * and safely deletes it.
     */
    private fun cleanIfOrphan(entity: TextDisplay) {
        val key = NamespacedKey(plugin, "worldboard")
        val boardId = entity.persistentDataContainer.get(key, PersistentDataType.STRING)
        
        if (boardId != null) {
            // It has our PDC tag. If it is NOT in the active registry, it's a ghost from a previous session!
            if (!activeBoards.containsKey(boardId)) {
                entity.remove()
                logger.log("Automatically swept orphaned WorldBoard entity: $boardId", LogLevel.LOW)
            }
        } else {
            // Legacy Cleanup Fallback: Check if the text matches the test commands to wipe the old untagged ghost!
            val rawText = entity.text()
            val plainText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(rawText)
            if (plainText.contains("WORLD BOARD TEST") || plainText.contains("wb_test") || plainText.contains("╭")) {
                entity.remove()
                logger.log("Automatically swept legacy untagged ghost WorldBoard entity", LogLevel.LOW)
            }
        }
    }
}

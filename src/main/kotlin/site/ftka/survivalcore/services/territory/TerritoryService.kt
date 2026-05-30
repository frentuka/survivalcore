package site.ftka.survivalcore.services.territory

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import site.ftka.survivalcore.services.ServicesFramework
import site.ftka.survivalcore.services.territory.subservices.Territory_StorageSubservice
import site.ftka.survivalcore.initless.proprietaryEvents.events.ChunkClaimedEvent
import site.ftka.survivalcore.initless.proprietaryEvents.events.ChunkUnclaimedEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class TerritoryService(private val plugin: MClass, private val services: ServicesFramework) {
    internal val logger = plugin.loggingInitless.getLog("Territory", Component.text("Territory").color(NamedTextColor.GREEN))
    
    // Global registry of all claimed chunks
    val claims = ConcurrentHashMap<Pair<Int, Int>, UUID>()

    internal val storage_ss = Territory_StorageSubservice(this, plugin)

    var isRestarting = false

    internal fun init() {
        logger.log("Initializing TerritoryService...", LogLevel.LOW)
        storage_ss.load()
    }

    internal fun restart() {
        logger.log("Restarting TerritoryService...", LogLevel.LOW)
        isRestarting = true
        storage_ss.save()
        claims.clear()
        storage_ss.load()
        isRestarting = false
    }

    internal fun stop() {
        logger.log("Stopping TerritoryService...", LogLevel.LOW)
        storage_ss.save()
    }

    fun claimChunk(uuid: UUID, chunkX: Int, chunkZ: Int): Boolean {
        val chunk = Pair(chunkX, chunkZ)
        if (claims.containsKey(chunk)) return false

        claims[chunk] = uuid
        storage_ss.saveAsync()

        val event = ChunkClaimedEvent(uuid, chunkX, chunkZ)
        plugin.propEventsInitless.fireEvent(event)
        
        // Also update player data
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            // Need to update the PlayerData, waiting for it to be accessible
            kotlinx.coroutines.runBlocking {
                services.playerData.inout_ss.makeModification(uuid) { data ->
                    if (!data.unlockedChunks.contains(chunk)) {
                        data.unlockedChunks.add(chunk)
                    }
                    true
                }
            }
        })
        return true
    }

    fun unclaimChunk(chunkX: Int, chunkZ: Int): Boolean {
        val chunk = Pair(chunkX, chunkZ)
        val owner = claims.remove(chunk) ?: return false

        storage_ss.saveAsync()

        val event = ChunkUnclaimedEvent(owner, chunkX, chunkZ)
        plugin.propEventsInitless.fireEvent(event)
        
        // Update player data
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            kotlinx.coroutines.runBlocking {
                services.playerData.inout_ss.makeModification(owner) { data ->
                    data.unlockedChunks.remove(chunk)
                    true
                }
            }
        })
        return true
    }

    fun getOwner(chunkX: Int, chunkZ: Int): UUID? {
        return claims[Pair(chunkX, chunkZ)]
    }
}

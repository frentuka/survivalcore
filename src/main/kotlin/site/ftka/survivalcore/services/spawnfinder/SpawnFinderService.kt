package site.ftka.survivalcore.services.spawnfinder

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import site.ftka.survivalcore.services.ServicesFramework
import site.ftka.survivalcore.services.spawnfinder.subservices.SpawnFinder_StorageSubservice
import site.ftka.survivalcore.services.spawnfinder.subservices.SpawnFinder_AlgorithmSubservice
import site.ftka.survivalcore.initless.proprietaryEvents.annotations.PropEventHandler
import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropListener
import site.ftka.survivalcore.initless.proprietaryEvents.events.ChunkClaimedEvent
import java.util.concurrent.CopyOnWriteArrayList

class SpawnFinderService(private val plugin: MClass, private val services: ServicesFramework) : PropListener {
    internal val logger = plugin.loggingInitless.getLog("SpawnFinder", Component.text("SpawnFinder").color(NamedTextColor.AQUA))
    
    val validSpawns = CopyOnWriteArrayList<Pair<Int, Int>>()
    
    internal val storage_ss = SpawnFinder_StorageSubservice(this, plugin)
    internal val algorithm_ss = SpawnFinder_AlgorithmSubservice(this, plugin)
    
    var isRestarting = false

    internal fun init() {
        logger.log("Initializing SpawnFinderService...", LogLevel.LOW)
        storage_ss.load()
        plugin.propEventsInitless.registerListener(this)
    }

    internal fun restart() {
        logger.log("Restarting SpawnFinderService...", LogLevel.LOW)
        isRestarting = true
        plugin.propEventsInitless.unregisterListener(this)
        storage_ss.save()
        validSpawns.clear()
        storage_ss.load()
        plugin.propEventsInitless.registerListener(this)
        isRestarting = false
    }

    internal fun stop() {
        logger.log("Stopping SpawnFinderService...", LogLevel.LOW)
        plugin.propEventsInitless.unregisterListener(this)
        storage_ss.save()
    }
    
    @PropEventHandler
    fun onChunkClaimed(event: ChunkClaimedEvent) {
        // If a new territory is claimed, check if any valid spawn is within 48 chunks
        val x = event.chunkX
        val z = event.chunkZ
        var changed = false
        
        val iterator = validSpawns.iterator()
        while(iterator.hasNext()) {
            val spawn = iterator.next()
            val distSq = (spawn.first - x) * (spawn.first - x) + (spawn.second - z) * (spawn.second - z)
            if (distSq <= 48 * 48) { // 48 chunks distance
                validSpawns.remove(spawn)
                changed = true
                logger.log("Removed valid spawn chunk ${spawn.first}, ${spawn.second} because it is too close to a newly claimed chunk at $x, $z.", LogLevel.DEBUG)
            }
        }
        if (changed) {
            storage_ss.saveAsync()
        }
    }
}

package site.ftka.survivalcore.services.gameplay.subservices

import org.bukkit.entity.Mob
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.world.ChunkLoadEvent
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.proprietaryEvents.annotations.PropEventHandler
import site.ftka.survivalcore.initless.proprietaryEvents.events.ChunkClaimedEvent
import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropListener
import site.ftka.survivalcore.services.gameplay.GameplayService

class Gameplay_EntityFreezeSubservice(private val service: GameplayService, private val plugin: MClass) : Listener, PropListener {

    fun init() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.propEventsInitless.registerListener(this)
    }

    fun restart() {
        plugin.propEventsInitless.unregisterListener(this)
        plugin.propEventsInitless.registerListener(this)
    }

    fun stop() {
        plugin.propEventsInitless.unregisterListener(this)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onChunkLoad(event: ChunkLoadEvent) {
        val chunk = event.chunk
        val owner = plugin.servicesFwk.territory.getOwner(chunk.x, chunk.z)
        
        if (owner == null) {
            // Freeze all mobs inside the chunk when it loads
            for (entity in chunk.entities) {
                if (entity is Mob) {
                    entity.setAware(false)
                }
            }
        } else {
            // Unfreeze just in case they were frozen
            for (entity in chunk.entities) {
                if (entity is Mob) {
                    entity.setAware(true)
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onCreatureSpawn(event: CreatureSpawnEvent) {
        val entity = event.entity
        val chunkX = entity.location.blockX shr 4
        val chunkZ = entity.location.blockZ shr 4
        
        val owner = plugin.servicesFwk.territory.getOwner(chunkX, chunkZ)
        if (owner == null) {
            // E.g. Chunk gen passive spawns
            if (entity is Mob) {
                entity.setAware(false)
            }
        }
    }

    @PropEventHandler
    fun onChunkClaimed(event: ChunkClaimedEvent) {
        val world = plugin.server.worlds.first() // Assuming overworld
        // Retrieve chunk asynchronously and apply changes safely
        world.getChunkAtAsync(event.chunkX, event.chunkZ).thenAccept { chunk ->
            // Running on the region scheduler for thread safety in Folia
            plugin.server.regionScheduler.execute(plugin, world, event.chunkX, event.chunkZ, Runnable {
                for (entity in chunk.entities) {
                    if (entity is Mob) {
                        entity.setAware(true)
                    }
                }
            })
        }
    }
}

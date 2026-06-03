package site.ftka.survivalcore.services.gameplay.subservices

import org.bukkit.entity.Monster
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityRemoveEvent
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.gameplay.GameplayService
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class Gameplay_MobSpawningSubservice(private val service: GameplayService, private val plugin: MClass) : Listener {

    // Fast O(1) tracker for hostile mob counts per player
    private val hostileMobCounts = ConcurrentHashMap<UUID, AtomicInteger>()

    fun init() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    fun restart() {
        hostileMobCounts.clear()
        // We do not unregister Bukkit events to avoid breaking other things, 
        // as Bukkit doesn't allow easy unregistering of a single listener instance without HandlerList.
        // It's generally safe as long as we clear state. 
        // Note: For a true restart, HandlerList.unregisterAll(this) could be used if needed.
    }

    fun stop() {
        hostileMobCounts.clear()
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onCreatureSpawn(event: CreatureSpawnEvent) {
        val entity = event.entity
        val chunkX = entity.location.blockX shr 4
        val chunkZ = entity.location.blockZ shr 4

        val owner = plugin.servicesFwk.territory.getOwner(chunkX, chunkZ)
        val isHostile = entity is Monster

        if (owner == null) {
            // Unclaimed Territory Rules:
            // 1. Hostile creatures: ONLY spawn inside player's area. -> Cancel all hostiles outside.
            if (isHostile) {
                event.isCancelled = true
                return
            }

            // 2. Non-hostile creatures naturally spawned: don't spawn outside player's region.
            if (event.spawnReason == CreatureSpawnEvent.SpawnReason.NATURAL || event.spawnReason == CreatureSpawnEvent.SpawnReason.DEFAULT) {
                event.isCancelled = true
                return
            }
            
            // 3. Non-hostile chunk gen spawns are allowed (and will be frozen by EntityFreezeSubservice)
        } else {
            // Claimed Territory Rules:
            if (isHostile) {
                // Check 15-minute grace period
                val playerData = plugin.servicesFwk.playerData.data.getPlayerData(owner)
                if (playerData != null) {
                    val firstConnection = playerData.information?.firstConnection ?: 0L
                    val timeSinceFirstConnection = System.currentTimeMillis() - firstConnection
                    
                    if (timeSinceFirstConnection < 15 * 60 * 1000) { // 15 minutes
                        event.isCancelled = true
                        return
                    }

                    // Check Custom Mob Cap
                    val unlockedChunks = playerData.unlockedChunks.size
                    val maxCap = Math.min(70, 5 * unlockedChunks)
                    
                    val currentCount = hostileMobCounts.computeIfAbsent(owner) { AtomicInteger(0) }.get()
                    
                    if (currentCount >= maxCap) {
                        event.isCancelled = true
                        return
                    }

                    // Allowed! Increment counter
                    hostileMobCounts[owner]?.incrementAndGet()
                    
                    // Mark the entity so we know who to decrement when it dies
                    entity.persistentDataContainer.set(
                        org.bukkit.NamespacedKey(plugin, "spawner_owner"),
                        org.bukkit.persistence.PersistentDataType.STRING,
                        owner.toString()
                    )
                }
            }
        }
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        decrementCounter(event.entity)
    }

    @EventHandler
    fun onEntityRemove(event: EntityRemoveEvent) {
        decrementCounter(event.entity)
    }

    private fun decrementCounter(entity: org.bukkit.entity.Entity) {
        if (entity !is Monster) return
        
        val key = org.bukkit.NamespacedKey(plugin, "spawner_owner")
        if (entity.persistentDataContainer.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
            val ownerStr = entity.persistentDataContainer.get(key, org.bukkit.persistence.PersistentDataType.STRING)
            if (ownerStr != null) {
                try {
                    val ownerUuid = UUID.fromString(ownerStr)
                    hostileMobCounts[ownerUuid]?.let {
                        if (it.get() > 0) {
                            it.decrementAndGet()
                        }
                    }
                } catch (e: Exception) {
                    // Ignore malformed UUIDs
                }
            }
        }
    }
}

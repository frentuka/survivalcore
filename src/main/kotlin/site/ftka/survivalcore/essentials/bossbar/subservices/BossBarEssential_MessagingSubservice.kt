package site.ftka.survivalcore.essentials.bossbar.subservices

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.bossbar.BossBarEssential
import site.ftka.survivalcore.essentials.bossbar.objects.ActiveBossBar
import site.ftka.survivalcore.essentials.bossbar.objects.BossBarLayer
import site.ftka.survivalcore.essentials.bossbar.objects.BossBarStrategy
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class BossBarEssential_MessagingSubservice(private val plugin: MClass, private val ess: BossBarEssential) {

    private val globalLayers = ConcurrentHashMap<String, BossBarLayer>()
    private val playerLayers = ConcurrentHashMap<UUID, ConcurrentHashMap<String, BossBarLayer>>()
    private val activeBars = ConcurrentHashMap<UUID, MutableList<ActiveBossBar>>()
    private var schedulerFuture: ScheduledFuture<*>? = null

    var strategy: BossBarStrategy = BossBarStrategy.STACK
    var maxStackedBars: Int = 3
    var carouselIntervalMs: Long = 2000L

    fun init() {
        startScheduler()
    }

    fun restart() {
        stop()
        init()
    }

    fun stop() {
        schedulerFuture?.cancel(true)
        schedulerFuture = null
        
        // Hide all active bars before clearing
        for ((uuid, bars) in activeBars) {
            val player = plugin.server.getPlayer(uuid)
            if (player != null) {
                bars.forEach { player.hideBossBar(it.adventureBar) }
            }
        }
        
        activeBars.clear()
        playerLayers.clear()
        globalLayers.clear()
    }

    fun setGlobalLayer(
        id: String,
        priority: Int,
        durationMs: Long?,
        colorProvider: () -> BossBar.Color,
        overlayProvider: () -> BossBar.Overlay,
        flagsProvider: () -> Set<BossBar.Flag>,
        progressProvider: () -> Float,
        titleProvider: () -> Component?
    ) {
        val expiresAt = durationMs?.let { System.currentTimeMillis() + it }
        val layer = BossBarLayer(id, priority, expiresAt, titleProvider, progressProvider, colorProvider, overlayProvider, flagsProvider)
        globalLayers[id] = layer
    }

    fun removeGlobalLayer(id: String) {
        globalLayers.remove(id)
    }

    fun clearGlobalLayers() {
        globalLayers.clear()
    }

    fun setLayer(
        uuid: UUID,
        id: String,
        priority: Int,
        durationMs: Long?,
        colorProvider: () -> BossBar.Color,
        overlayProvider: () -> BossBar.Overlay,
        flagsProvider: () -> Set<BossBar.Flag>,
        progressProvider: () -> Float,
        titleProvider: () -> Component?
    ) {
        val expiresAt = durationMs?.let { System.currentTimeMillis() + it }
        val layer = BossBarLayer(id, priority, expiresAt, titleProvider, progressProvider, colorProvider, overlayProvider, flagsProvider)
        playerLayers.computeIfAbsent(uuid) { ConcurrentHashMap() }[id] = layer
    }

    fun removeLayer(uuid: UUID, id: String) {
        playerLayers[uuid]?.remove(id)
    }

    fun clearLayers(uuid: UUID) {
        playerLayers.remove(uuid)
        hideAllActiveBars(uuid)
    }

    private fun hideAllActiveBars(uuid: UUID) {
        val bars = activeBars.remove(uuid)
        if (bars != null) {
            val player = plugin.server.getPlayer(uuid)
            if (player != null) {
                bars.forEach { player.hideBossBar(it.adventureBar) }
            }
        }
    }

    private fun startScheduler() {
        if (schedulerFuture != null) return

        schedulerFuture = plugin.globalScheduler.scheduleAtFixedRate({
            try {
                renderAllActiveBossBars()
            } catch (e: Exception) {
                ess.logger.log("Error in BossBar render task: ${e.message}")
            }
        }, 100, 100, TimeUnit.MILLISECONDS)
    }

    private fun renderAllActiveBossBars() {
        val currentTime = System.currentTimeMillis()

        // 1. Process Global Layers once per tick
        val activeGlobalLayers = mutableListOf<BossBarLayer>()
        val globalIterator = globalLayers.values.iterator()
        while (globalIterator.hasNext()) {
            val layer = globalIterator.next()
            if (layer.isExpired()) {
                globalIterator.remove()
            } else {
                activeGlobalLayers.add(layer)
            }
        }

        // 2. Process per-player Layers
        for (player in plugin.server.onlinePlayers) {
            val uuid = player.uniqueId
            val layersMap = playerLayers[uuid]
            
            val validLayers = mutableListOf<BossBarLayer>()
            validLayers.addAll(activeGlobalLayers) // Start with global layers

            if (layersMap != null) {
                val personalIterator = layersMap.values.iterator()
                while (personalIterator.hasNext()) {
                    val layer = personalIterator.next()
                    if (layer.isExpired()) {
                        personalIterator.remove()
                    } else {
                        // Avoid duplicates if a personal layer overrides a global one by ID
                        if (validLayers.none { it.id == layer.id }) {
                            validLayers.add(layer)
                        } else {
                            // Replace global with personal override
                            validLayers.removeIf { it.id == layer.id }
                            validLayers.add(layer)
                        }
                    }
                }
                
                // Cleanup empty maps to prevent memory leaks
                if (layersMap.isEmpty()) {
                    playerLayers.remove(uuid)
                }
            }

            if (validLayers.isEmpty()) {
                hideAllActiveBars(uuid)
                continue
            }

            // Sort layers by priority descending
            validLayers.sortByDescending { it.priority }

            // Determine target layers based on strategy
            val targetLayers = when (strategy) {
                BossBarStrategy.EXCLUSIVE -> listOf(validLayers.first())
                BossBarStrategy.STACK -> validLayers.take(maxStackedBars)
                BossBarStrategy.CAROUSEL -> {
                    val slideIndex = ((currentTime / carouselIntervalMs) % validLayers.size).toInt()
                    listOf(validLayers[slideIndex])
                }
            }

            val targetLayerIds = targetLayers.map { it.id }.toSet()
            val currentActiveBars = activeBars.computeIfAbsent(uuid) { mutableListOf() }

            // Hide and remove bars that are no longer targeted
            val barsIterator = currentActiveBars.iterator()
            while (barsIterator.hasNext()) {
                val activeBar = barsIterator.next()
                if (activeBar.layerId !in targetLayerIds) {
                    player.hideBossBar(activeBar.adventureBar)
                    barsIterator.remove()
                }
            }

            // Add or update target layers
            for (layer in targetLayers) {
                var title: Component? = null
                var progress = 0.0f
                var color = BossBar.Color.PURPLE
                var overlay = BossBar.Overlay.PROGRESS
                var flags = emptySet<BossBar.Flag>()

                try {
                    title = layer.titleProvider()
                    progress = layer.progressProvider().coerceIn(0.0f, 1.0f)
                    color = layer.colorProvider()
                    overlay = layer.overlayProvider()
                    flags = layer.flagsProvider()
                } catch (e: Exception) {
                    ess.logger.log("Error evaluating BossBar layer '${layer.id}' for player ${player.name}: ${e.message}")
                    continue
                }

                val existingBar = currentActiveBars.find { it.layerId == layer.id }

                if (title == null) {
                    // Graceful hiding: If title becomes null, treat as temporarily inactive and hide
                    if (existingBar != null) {
                        player.hideBossBar(existingBar.adventureBar)
                        currentActiveBars.remove(existingBar)
                    }
                    continue
                }

                if (existingBar != null) {
                    // Update properties if changed
                    if (existingBar.adventureBar.name() != title) existingBar.adventureBar.name(title)
                    if (existingBar.adventureBar.progress() != progress) existingBar.adventureBar.progress(progress)
                    if (existingBar.adventureBar.color() != color) existingBar.adventureBar.color(color)
                    if (existingBar.adventureBar.overlay() != overlay) existingBar.adventureBar.overlay(overlay)
                    if (existingBar.adventureBar.flags() != flags) existingBar.adventureBar.flags(flags)
                } else {
                    // Create and show new bar
                    val adventureBar = BossBar.bossBar(title, progress, color, overlay, flags)
                    val newActiveBar = ActiveBossBar(layer.id, adventureBar)
                    currentActiveBars.add(newActiveBar)
                    player.showBossBar(adventureBar)
                }
            }
        }
    }
}

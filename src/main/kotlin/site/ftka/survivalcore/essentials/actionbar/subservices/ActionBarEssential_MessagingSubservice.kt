package site.ftka.survivalcore.essentials.actionbar.subservices

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.actionbar.ActionBarEssential
import site.ftka.survivalcore.essentials.actionbar.objects.ActionBarLayer
import site.ftka.survivalcore.essentials.actionbar.objects.ActionBarStrategy
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ActionBarEssential_MessagingSubservice(private val plugin: MClass, private val ess: ActionBarEssential) {

    private val playerLayers = ConcurrentHashMap<UUID, ConcurrentHashMap<String, ActionBarLayer>>()
    private var schedulerFuture: ScheduledFuture<*>? = null

    // Strategy Configuration
    var strategy: ActionBarStrategy = ActionBarStrategy.CONCATENATION
    var carouselIntervalMs: Long = 2000L // 2 seconds per slide

    // Separator component for concatenation strategy: "  |  " in gray color
    private val separatorComponent = Component.text("  |  ").color(NamedTextColor.GRAY)

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
        playerLayers.clear()
    }

    /**
     * Sends a direct action bar message (compatibility mode).
     * Registers a short-lived layer that expires after 3 seconds with high priority.
     */
    fun sendActionBar(uuid: UUID, message: Component) {
        setLayer(uuid, "direct_message", 1000, 3000L) { message }
    }

    /**
     * Sets or updates a dynamic action bar layer for a player.
     *
     * @param uuid The player's UUID.
     * @param id The unique identifier for this layer.
     * @param priority Higher priority layers are sorted first or displayed exclusively.
     * @param durationMs Optional duration in milliseconds after which the layer expires (null for permanent).
     * @param provider Lambda returning the adventure Component to display, or null if temporarily inactive.
     */
    fun setLayer(uuid: UUID, id: String, priority: Int, durationMs: Long?, provider: () -> Component?) {
        val expiresAt = durationMs?.let { System.currentTimeMillis() + it }
        val layer = ActionBarLayer(id, priority, expiresAt, provider)
        
        playerLayers.computeIfAbsent(uuid) { ConcurrentHashMap() }[id] = layer
    }

    /**
     * Removes an active layer from the player's stack.
     */
    fun removeLayer(uuid: UUID, id: String) {
        playerLayers[uuid]?.remove(id)
    }

    /**
     * Clears all registered layers for a player.
     */
    fun clearLayers(uuid: UUID) {
        playerLayers.remove(uuid)
    }

    /**
     * Starts the asynchronous rendering ticker.
     */
    private fun startScheduler() {
        if (schedulerFuture != null) return

        schedulerFuture = plugin.globalScheduler.scheduleAtFixedRate({
            try {
                renderAllActiveActionBars()
            } catch (e: Exception) {
                ess.logger.log("Error in ActionBar render task: ${e.message}")
            }
        }, 100, 100, TimeUnit.MILLISECONDS) // Run every 100ms
    }

    /**
     * Renders and sends composite action bar messages to all online players with active layers.
     */
    private fun renderAllActiveActionBars() {
        val currentTime = System.currentTimeMillis()
        
        for (player in plugin.server.onlinePlayers) {
            val uuid = player.uniqueId
            val layersMap = playerLayers[uuid] ?: continue
            if (layersMap.isEmpty()) continue

            // 1. Gather active layers and remove expired ones
            val activeLayers = mutableListOf<ActionBarLayer>()

            val iterator = layersMap.values.iterator()
            while (iterator.hasNext()) {
                val layer = iterator.next()
                if (layer.isExpired()) {
                    iterator.remove()
                } else {
                    activeLayers.add(layer)
                }
            }

            if (activeLayers.isEmpty()) continue

            // 2. Sort layers by priority descending (highest priority first)
            activeLayers.sortByDescending { it.priority }

            // 3. Process according to selected Strategy
            val compositeActionBar: Component = when (strategy) {
                ActionBarStrategy.EXCLUSIVE -> {
                    // Only render the absolute highest priority layer
                    val highestLayer = activeLayers.first()
                    try {
                        highestLayer.provider() ?: Component.empty()
                    } catch (e: Exception) {
                        ess.logger.log("Error rendering layer '${highestLayer.id}' for player ${player.name}: ${e.message}")
                        Component.empty()
                    }
                }
                ActionBarStrategy.CAROUSEL -> {
                    // Cycle through layers based on time
                    val slideIndex = ((currentTime / carouselIntervalMs) % activeLayers.size).toInt()
                    val slideLayer = activeLayers[slideIndex]
                    try {
                        slideLayer.provider() ?: Component.empty()
                    } catch (e: Exception) {
                        ess.logger.log("Error rendering layer '${slideLayer.id}' for player ${player.name}: ${e.message}")
                        Component.empty()
                    }
                }
                ActionBarStrategy.CONCATENATION -> {
                    // Collect non-null rendered Components and join them
                    val renderedComponents = activeLayers.mapNotNull { layer ->
                        try {
                            layer.provider()
                        } catch (e: Exception) {
                            ess.logger.log("Error rendering layer '${layer.id}' for player ${player.name}: ${e.message}")
                            null
                        }
                    }

                    if (renderedComponents.isEmpty()) Component.empty()
                    else Component.join(JoinConfiguration.separator(separatorComponent), renderedComponents)
                }
            }

            // 4. Send to player
            if (compositeActionBar != Component.empty()) {
                player.sendActionBar(compositeActionBar)
            }
        }
    }
}
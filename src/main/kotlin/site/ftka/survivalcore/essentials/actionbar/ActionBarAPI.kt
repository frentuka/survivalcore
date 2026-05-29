package site.ftka.survivalcore.essentials.actionbar

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.essentials.actionbar.objects.ActionBarStrategy
import java.util.UUID

class ActionBarAPI(private val ess: ActionBarEssential) {

    /**
     * Gets or sets the current rendering strategy for multiplexing action bars.
     * Strategies include: CONCATENATION, EXCLUSIVE, CAROUSEL.
     */
    var strategy: ActionBarStrategy
        get() = ess.messaging_ss.strategy
        set(value) { ess.messaging_ss.strategy = value }

    /**
     * Gets or sets the interval (in milliseconds) used for the CAROUSEL rendering strategy.
     * Default is 2000 (2 seconds).
     */
    var carouselIntervalMs: Long
        get() = ess.messaging_ss.carouselIntervalMs
        set(value) { ess.messaging_ss.carouselIntervalMs = value }

    /**
     * Sends an action bar message to the player (compatibility mode).
     *
     * @param uuid The player's UUID.
     * @param message The message to send.
     */
    fun sendActionBar(uuid: UUID, message: Component) {
        ess.messaging_ss.sendActionBar(uuid, message)
    }

    /**
     * Registers or updates an active layer for a player.
     *
     * @param uuid The player's UUID.
     * @param id The unique identifier for this layer (e.g. "combat_tag", "status_hud").
     * @param priority Higher priority layers are sorted first or displayed exclusively.
     * @param durationMs Optional duration in milliseconds after which the layer expires (null for permanent).
     * @param provider Lambda returning the adventure Component to display, or null if temporarily inactive.
     */
    fun setLayer(
        uuid: UUID,
        id: String,
        priority: Int,
        durationMs: Long? = null,
        provider: () -> Component?
    ) {
        ess.messaging_ss.setLayer(uuid, id, priority, durationMs, provider)
    }

    /**
     * Removes an active layer from the player's stack.
     *
     * @param uuid The player's UUID.
     * @param id The layer ID to remove.
     */
    fun removeLayer(uuid: UUID, id: String) {
        ess.messaging_ss.removeLayer(uuid, id)
    }

    /**
     * Removes all active layers for a player.
     *
     * @param uuid The player's UUID.
     */
    fun clearLayers(uuid: UUID) {
        ess.messaging_ss.clearLayers(uuid)
    }

}
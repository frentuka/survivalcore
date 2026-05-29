package site.ftka.survivalcore.essentials.bossbar

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import site.ftka.survivalcore.essentials.bossbar.objects.BossBarStrategy
import java.util.UUID

class BossBarAPI(private val ess: BossBarEssential) {

    var strategy: BossBarStrategy
        get() = ess.messaging_ss.strategy
        set(value) { ess.messaging_ss.strategy = value }

    var maxStackedBars: Int
        get() = ess.messaging_ss.maxStackedBars
        set(value) { ess.messaging_ss.maxStackedBars = value }

    var carouselIntervalMs: Long
        get() = ess.messaging_ss.carouselIntervalMs
        set(value) { ess.messaging_ss.carouselIntervalMs = value }

    /**
     * Registers a global bossbar layer visible to ALL online players.
     * Perfect for server-wide events, restarts, and announcements.
     */
    fun setGlobalLayer(
        id: String,
        priority: Int,
        durationMs: Long? = null,
        color: () -> BossBar.Color = { BossBar.Color.PURPLE },
        overlay: () -> BossBar.Overlay = { BossBar.Overlay.PROGRESS },
        flags: () -> Set<BossBar.Flag> = { emptySet() },
        progress: () -> Float,
        title: () -> Component?
    ) {
        ess.messaging_ss.setGlobalLayer(id, priority, durationMs, color, overlay, flags, progress, title)
    }

    fun removeGlobalLayer(id: String) {
        ess.messaging_ss.removeGlobalLayer(id)
    }

    fun clearGlobalLayers() {
        ess.messaging_ss.clearGlobalLayers()
    }

    /**
     * Registers a player-specific bossbar layer.
     * If it shares the same ID as a global layer, it will override the global layer for this specific player.
     */
    fun setLayer(
        uuid: UUID,
        id: String,
        priority: Int,
        durationMs: Long? = null,
        color: () -> BossBar.Color = { BossBar.Color.PURPLE },
        overlay: () -> BossBar.Overlay = { BossBar.Overlay.PROGRESS },
        flags: () -> Set<BossBar.Flag> = { emptySet() },
        progress: () -> Float,
        title: () -> Component?
    ) {
        ess.messaging_ss.setLayer(uuid, id, priority, durationMs, color, overlay, flags, progress, title)
    }

    fun removeLayer(uuid: UUID, id: String) {
        ess.messaging_ss.removeLayer(uuid, id)
    }

    fun clearLayers(uuid: UUID) {
        ess.messaging_ss.clearLayers(uuid)
    }
}

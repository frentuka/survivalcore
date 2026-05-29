package site.ftka.survivalcore.essentials.bossbar.objects

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component

data class BossBarLayer(
    val id: String,
    val priority: Int,
    val expiresAt: Long?,
    val titleProvider: () -> Component?,
    val progressProvider: () -> Float,
    val colorProvider: () -> BossBar.Color,
    val overlayProvider: () -> BossBar.Overlay,
    val flagsProvider: () -> Set<BossBar.Flag>
) {
    fun isExpired(): Boolean {
        if (expiresAt == null) return false
        return System.currentTimeMillis() >= expiresAt
    }
}

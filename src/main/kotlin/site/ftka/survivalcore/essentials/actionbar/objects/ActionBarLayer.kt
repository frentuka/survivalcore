package site.ftka.survivalcore.essentials.actionbar.objects

import net.kyori.adventure.text.Component

data class ActionBarLayer(
    val id: String,
    val priority: Int,
    val expiresAt: Long?, // System.currentTimeMillis() epoch (null = infinite)
    val provider: () -> Component?
) {
    /**
     * Checks if the layer has expired based on current system time.
     */
    fun isExpired(): Boolean {
        if (expiresAt == null) return false
        return System.currentTimeMillis() >= expiresAt
    }
}

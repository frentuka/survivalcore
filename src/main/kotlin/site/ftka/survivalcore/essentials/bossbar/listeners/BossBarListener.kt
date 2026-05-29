package site.ftka.survivalcore.essentials.bossbar.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.bossbar.BossBarEssential

class BossBarListener(private val plugin: MClass, private val ess: BossBarEssential) : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        ess.messaging_ss.clearLayers(event.player.uniqueId)
    }
}

package site.ftka.survivalcore.essentials.actionbar.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.actionbar.ActionBarEssential

class ActionBarListener(private val plugin: MClass, private val ess: ActionBarEssential) : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        ess.messaging_ss.clearLayers(event.player.uniqueId)
    }
}

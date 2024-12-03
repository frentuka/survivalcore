package site.ftka.survivalcore.essentials.usernameTracker.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import site.ftka.survivalcore.essentials.usernameTracker.UsernameTrackerEssential

class UsernameTrackerEssential_Listener(private val ess: UsernameTrackerEssential): Listener {

    @EventHandler
    fun onPreLogin(event: PlayerLoginEvent) {
        val uuid = event.player.uniqueId
        val name = event.player.name
        ess.addUsername(uuid, name)
    }

}
package site.ftka.survivalcore.essentials.usernameTracker.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.usernameTracker.UsernameTrackerEssential

/*
    should never be public
    it's only intended use is inside this project's modules
 */
internal class UsernameTrackerEssential_Listener(private val ess: UsernameTrackerEssential, private val plugin: MClass): Listener {

    @EventHandler
    fun onLogin(event: PlayerLoginEvent) {
        val uuid = event.player.uniqueId
        val name = event.player.name

        /*
        is a player with the exact same name already connected?
        if it is, then it's name should really be it's old name
        and the player who's connecting is the only one with that name for real
         */

        if (plugin.servicesFwk.playerData.data.getOnlinePlayers().containsValue(name)) {
            val matchingUsernamesMap = plugin.servicesFwk.playerData.data.getOnlinePlayers().filterValues { it == name }.keys

            for (matchingUUID in matchingUsernamesMap)
                if (uuid != matchingUUID) {
                    plugin.servicesFwk.playerData.data.removeOnlinePlayer(uuid)

                    // todo: pretty this up. some sort of message is needed
                    plugin.server.getPlayer(matchingUUID)?.player?.kick()
                }
        }

        ess.addUsername(uuid, name)
    }

}
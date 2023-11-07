package site.ftka.survivalcore.services.playerstate.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.events.*
import site.ftka.survivalcore.services.playerstate.PlayerStateService

class PlayerStateListener(private val service: PlayerStateService, private val plugin: MClass): Listener {


    @EventHandler
    fun onPlayerDataInit(event: PlayerDataInitEvent) {

    }

    @EventHandler
    fun onPlayerDataRestart(event: PlayerDataRestartEvent) {

    }

    @EventHandler
    fun onPlayerDataRegister(event: PlayerDataRegisterEvent) {
        println("PLAYERSTATE PLAYERDATAREGISTEREVENT")

        event.playerdata?.let {
            println("PLAYERDATA NOT NULL")
            val player = plugin.server.getPlayer(it.uuid)
            player?.let{
                println("PLAYER NOT NULL")
                event.playerdata.state.gatherValuesFromPlayer(it)
            }
        }
    }

    @EventHandler
    fun onPlayerDataPreUnregister(event: PlayerDataPreUnregisterEvent) {
        // This event's playerdata is
    }

}
package site.ftka.survivalcore.services.permissions.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerKickEvent
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.proprietaryEvents.annotations.PropEventHandler
import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropListener
import site.ftka.survivalcore.services.permissions.PermissionsService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRegisterEvent

class PermissionsServiceListener(private val service: PermissionsService, private val plugin: MClass) : Listener, PropListener {

    @PropEventHandler
    fun onPlayerDataRegister(event: PlayerDataRegisterEvent) {
        val player = plugin.server.getPlayer(event.uuid) ?: return
        service.players_ss.refreshAttachment(player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        service.players_ss.removeAttachment(event.player)
    }

    @EventHandler
    fun onPlayerKick(event: PlayerKickEvent) {
        service.players_ss.removeAttachment(event.player)
    }
}

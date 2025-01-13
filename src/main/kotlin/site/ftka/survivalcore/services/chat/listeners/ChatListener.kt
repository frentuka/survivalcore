package site.ftka.survivalcore.services.chat.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropListener
import site.ftka.survivalcore.services.chat.ChatService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRegisterEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataUnregisterEvent
import java.util.UUID

class ChatListener(private val svc: ChatService, private val plugin: MClass): Listener, PropListener {
    private val logger = svc.logger.sub("Listener")

    @EventHandler
    fun onPDJoin(ev: PlayerDataRegisterEvent) {
        logger.log("Adding default active channels for ${ev.uuid}")

        // add player to it's personal channel
        svc.channels_ss.addActiveChannel(ev.uuid, svc.channels_ss.getPlayerChannel(ev.uuid, true)!!.name)

        // add player to global channel
        svc.channels_ss.getGlobalChannel()?.name?.let { svc.channels_ss.addActiveChannel(ev.uuid, it) }

        // add player to staff channel if they have staff permissions
        if (plugin.servicesFwk.permissions.api.playerHasPerm(ev.uuid, "staff.*"))
            svc.channels_ss.getStaffChannel()?.name?.let { svc.channels_ss.addActiveChannel(ev.uuid, it) }
    }

    @EventHandler
    fun onPDQuit(ev: PlayerDataUnregisterEvent) {
        svc.channels_ss.removeActiveChannels(ev.uuid)
    }

    // purge player's data after certain amount of time
    private fun purgeDataTimeout(uuid: UUID) {
        svc.channels_ss.removeActiveChannels(uuid)
    }

}
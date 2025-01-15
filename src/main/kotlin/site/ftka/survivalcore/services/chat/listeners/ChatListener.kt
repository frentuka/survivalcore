package site.ftka.survivalcore.services.chat.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.proprietaryEvents.annotations.PropEventHandler
import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropListener
import site.ftka.survivalcore.services.chat.ChatService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRegisterEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataUnregisterEvent
import java.util.UUID

class ChatListener(private val svc: ChatService, private val plugin: MClass): Listener, PropListener {
    private val logger = svc.logger.sub("Listener")

    private var asd = true

    @EventHandler
    fun onChat(ev: AsyncChatEvent) {
        // cancel vanilla event so control is fully mine
        ev.isCancelled = true

        // send chat message as common. should do some checks beforehand but will implement them later
        svc.messaging_ss.sendGlobalMessage(Component.text("${ev.player.name()} > ${ev.message()}"))
    }

    @PropEventHandler
    fun onPDJoin(ev: PlayerDataRegisterEvent) {
        logger.log("Adding default active channels for ${ev.uuid}")

        // add player to it's personal channel
        svc.channels_ss.addActiveChannel(ev.uuid, svc.channels_ss.getPlayerChannel(ev.uuid, true)!!.name)

        // add player to global channel
        svc.channels_ss.getGlobalChannel()?.name?.let { svc.channels_ss.addActiveChannel(ev.uuid, it) }

        // add player to staff channel if they have staff permissions
        if (plugin.servicesFwk.permissions.api.playerHasPerm(ev.uuid, "staff.*"))
            svc.channels_ss.getStaffChannel()?.name?.let { svc.channels_ss.addActiveChannel(ev.uuid, it) }

        // restore corresponding chat to player
        svc.messaging_ss.restorePlayerChat(ev.uuid, 11)
    }

    @PropEventHandler
    fun onPDQuit(ev: PlayerDataUnregisterEvent) {
        svc.channels_ss.removeActiveChannels(ev.uuid)
    }

    // purge player's data after certain amount of time
    private fun purgeDataTimeout(uuid: UUID) {
        svc.channels_ss.removeActiveChannels(uuid)
    }

}
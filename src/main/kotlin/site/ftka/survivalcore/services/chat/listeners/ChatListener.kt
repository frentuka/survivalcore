package site.ftka.survivalcore.services.chat.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.proprietaryEvents.annotations.PropEventHandler
import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropListener
import site.ftka.survivalcore.services.chat.ChatService
import site.ftka.survivalcore.services.chat.events.ChatService_ChatEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRegisterEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataUnregisterEvent

internal class ChatListener(private val svc: ChatService, private val plugin: MClass) : Listener, PropListener {
    private val logger = svc.logger.sub("Listener")

    @EventHandler
    fun onChat(ev: AsyncChatEvent) {
        // cancel vanilla event so control is fully mine
        ev.isCancelled = true

        val uuid = ev.player.uniqueId

        // if chat is sent from a screen, trigger corresponding event. don't trigger chat event
        if (svc.screens_ss.isPlayerInsideScreen(uuid)) {
            // execute listener
            svc.screens_ss.getActiveScreen(uuid)?.getCurrentChatScreenPageObject()?.onChat?.let { it(ev.signedMessage().message(), ev.player) }
            return
        }

        // if player is not in global channel, don't send any message
        // todo: let players send messages to other channels
        if (!svc.api.getPlayerActiveChannels(uuid).contains(svc.channels_ss.getGlobalChannel().name))
            return

        val event = ChatService_ChatEvent(uuid, ev.message())
        plugin.propEventsInitless.fireEvent(event)

        /*
            send chat message
         */

        val msg = plugin.servicesFwk.language.api.player_chatMessagePrefix(uuid)
            .append(Component.text(" ")).append(event.message.color(NamedTextColor.WHITE))

        svc.messaging_ss.sendGlobalMessage(msg)
    }

    /*
        playerdata
     */

    @PropEventHandler
    fun onPDJoin(ev: PlayerDataRegisterEvent) {
        logger.log("Adding default active channels for ${ev.uuid}")

        // add player to global channel
        svc.channels_ss.addActiveChannel(ev.uuid, svc.channels_ss.getGlobalChannel().name)

        // add player to it's personal channel
        val playerName = plugin.essentialsFwk.usernameTracker.getName(ev.uuid)
        playerName?.let { svc.api.addActiveChannel(ev.uuid, playerName) }


        // add player to staff channel if they have staff permissions
        if (plugin.servicesFwk.permissions.api.playerHasPerm(ev.uuid, "staff.*"))
            svc.channels_ss.addActiveChannel(ev.uuid, svc.channels_ss.getStaffChannel().name)

        // restore corresponding chat to player
        svc.messaging_ss.restorePlayerChat(ev.uuid, 100)
    }

    @PropEventHandler
    fun onPDQuit(ev: PlayerDataUnregisterEvent) {
        svc.channels_ss.purgePlayer(ev.uuid)
        svc.screens_ss.stopAnyScreen(ev.uuid)
    }

}
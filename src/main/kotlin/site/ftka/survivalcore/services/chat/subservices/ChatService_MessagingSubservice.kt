
package site.ftka.survivalcore.services.chat.subservices

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chat.ChatService
import java.util.UUID

internal class ChatService_MessagingSubservice(private val svc: ChatService, private val plugin: MClass) {

    // returns last {entries} player's chat log messages
    private fun getPlayerChatLog(uuid: UUID, entries: Int): Map<Long, Component> {
        val activeChannels = svc.channels_ss.getActiveChannels(uuid)
        val messagesMap = mutableMapOf<Long, Component>()

        // add all active channels messages into a map
        for (channelName in activeChannels)
            messagesMap.putAll(svc.channels_ss.getChannel(channelName)?.getLatestMessages(entries)
                ?: run { // if null, remove "active" map
                    svc.channels_ss.removeActiveChannel(uuid, channelName)
                    emptyMap()
                })

        // take only highest {entries} entries
        return messagesMap.toSortedMap().entries.take(entries).associate { it.key to it.value }
    }

    // sends the chat back to player
    internal fun restorePlayerChat(uuid: UUID, entries: Int) {
        val chatLog = getPlayerChatLog(uuid, entries)

        // send messages starting from lowest to highest key
        for (message in chatLog.values)
            sendChannellessMessage(uuid, message, true)
    }

    // sends a 100-lines-long blank text message
    internal fun clearChat(uuid: UUID) {
        // create text before sending
        var blankText = " \n "
        // repeat the same text 50 times
        for (i in 0..50)
            blankText += " \n "

        // send
        sendChannellessMessage(uuid, Component.text(blankText), false)
    }

    internal fun sendGlobalMessage(message: Component) {
        svc.channels_ss.getGlobalChannel()?.addMessage(message)
        for (player in plugin.server.onlinePlayers)
            sendChannellessMessage(player.uniqueId, message, true)
    }

    // send message to player
    internal fun sendMessage(uuid: UUID, message: Component, respectScreens: Boolean = true) {
        svc.channels_ss.getPlayerChannel(uuid)?.addMessage(message)
        sendChannellessMessage(uuid, message, true)
    }

    // send message to channel
    internal fun sendMessage(channelName: String, message: Component) {
        svc.channels_ss.getChannel(channelName)?.let {
            it.addMessage(message)
            for (uuid in it.members) {
                // check if player has channel active
                if (svc.channels_ss.getActiveChannels(uuid).contains(channelName))
                    sendChannellessMessage(uuid, message, true) // player is active on channel so message will be sent
                else // remove player from channel if he's not active
                    svc.channels_ss.modifyChannel(channelName) { it.members.remove(uuid) }
            }
        }
    }

    internal fun sendChannellessMessage(uuid: UUID, message: Component, respectScreens: Boolean = true) {
        if (respectScreens and svc.screens_ss.isPlayerInsideScreen(uuid))
            return

        val player = plugin.server.getPlayer(uuid) ?: return
        player.sendMessage(message)
    }

}
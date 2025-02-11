
package site.ftka.survivalcore.essentials.chat.subservices

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.chat.ChatEssential
import java.util.UUID

internal class ChatEssential_MessagingSubservice(private val svc: ChatEssential, private val plugin: MClass) {

    // returns last {entries} player's chat log messages
    fun getPlayerChatLog(uuid: UUID, entries: Int): Map<Long, Component> {
        val activeChannels = svc.channels_ss.getActiveChannels(uuid)
        val messagesMap = mutableMapOf<Long, Component>()

        // add all active channels messages into a map
        for (channelName in activeChannels) {
            messagesMap.putAll(
                svc.channels_ss.getChannel(channelName)?.getLatestMessages(entries)
                    ?: run { // if null, remove "active" map
                        svc.channels_ss.removeActiveChannel(uuid, channelName)
                        emptyMap()
                    })
        }

        // take only highest {entries} entries
        return messagesMap.toSortedMap().entries.take(entries).associate { it.key to it.value }
    }

    // sends the chat back to player
    fun restorePlayerChat(uuid: UUID, entries: Int) {
        val chatLog = getPlayerChatLog(uuid, entries)

        // send messages starting from lowest to highest key
        for (message in chatLog.values)
            sendChannellessMessage(uuid, message, true)
    }

    // sends a 100-lines-long blank text message
    fun clearChat(uuid: UUID) {
        // create text before sending
        var blankText = " \n "
        // repeat the same text 50 times
        for (i in 0..50)
            blankText += " \n "

        // send
        sendChannellessMessage(uuid, Component.text(blankText), false)
    }

    // send message to global channel
    fun sendGlobalMessage(message: Component) {
        svc.channels_ss.getGlobalChannel().addMessage(message)

        for (player in plugin.server.onlinePlayers)
            if (svc.channels_ss.getActiveChannels(player.uniqueId).contains(svc.channels_ss.getGlobalChannel().name))
                sendChannellessMessage(player.uniqueId, message, true)
    }

    // send message to staff channel
    fun sendStaffMessage(message: Component) {
        svc.channels_ss.getStaffChannel().addMessage(message)
        for (player in plugin.server.onlinePlayers)
            if (player.hasPermission("survivalcore.staff"))
                sendChannellessMessage(player.uniqueId, message, true)
    }

    // send message to player's personal channel
    fun sendPersonalMessage(uuid: UUID, message: Component, respectScreens: Boolean = true) {
        val player = plugin.server.getPlayer(uuid) ?: return
        svc.channels_ss.getPlayerChannel(uuid, true)?.addMessage(message)
        sendChannellessMessage(uuid, message, respectScreens)
    }

    // send message to channel
    fun sendChannelMessage(channelName: String, message: Component) {
        svc.channels_ss.getChannel(channelName)?.let {
            it.addMessage(message)
            for (uuid in it.members) {
                // check if player has channel active
                if (svc.channels_ss.getActiveChannels(uuid).contains(channelName))
                    sendChannellessMessage(uuid, message, true) // player is active on channel so message will be sent
                else // remove player from channel as it's not active
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
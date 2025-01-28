package site.ftka.survivalcore.services.chat

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.services.chat.objects.ChatChannel
import site.ftka.survivalcore.services.chat.objects.ChatScreen
import java.util.UUID

class ChatAPI(private val svc: ChatService) {

    // channel administration

    /**
     * Creates a channel by its name
     *
     * @param name The name of the channel
     * @return The created channel
     */
    fun createChannel(name: String) {
        svc.channels_ss.registerChannel(name)
    }

    /**
     * Modifies a channel by its name
     *
     * @param name The name of the channel
     * @param modification The modification to apply
     */
    fun modifyChannel(name: String, modification: (ChatChannel) -> Unit) {
        svc.channels_ss.modifyChannel(name, modification)
    }

    /**
     * Removes a channel by its name
     *
     * @param name The name of the channel
     */
    fun removeChannel(name: String) {
        svc.channels_ss.removeChannel(name)
    }

    // channel getters

    /**
     * Gets a channel by its name
     *
     * @param name The name of the channel
     * @return The channel
     */
    fun getChannel(name: String)
        = svc.channels_ss.getChannel(name)

    /**
     * Gets the global channel
     *
     * @return The global channel
     */
    fun getGlobalChannel()
        = svc.channels_ss.getGlobalChannel()

    /**
     * Gets the staff channel
     *
     * @return The staff channel
     */
    fun getStaffChannel()
        = svc.channels_ss.getStaffChannel()

    /**
     * Gets the player's active channels
     *
     * @param uuid The player's UUID
     * @return The player's personal channel
     */
    fun getPlayerChannel(uuid: UUID)
        = svc.channels_ss.getPlayerChannel(uuid)

    // screens

    /**
     * Shows a screen to a player
     *
     * @param uuid The player's UUID
     * @param screen The screen to show
     */
    fun showScreen(uuid: UUID, screen: ChatScreen)
        = svc.screens_ss.showScreen(uuid, screen)

    /**
     * Refreshes the screen of a player
     *
     * @param uuid The player's UUID
     * @param page The new page to show
     */
    fun refreshScreen(uuid: UUID, page: String)
        = svc.screens_ss.refreshScreen(uuid, page)

    /**
     * Stops a specific screen that the player is currently in
     * Meant to prevent screen A from stopping screen B
     *
     * @param uuid The player's UUID
     * @param name The screen's name
     */
    fun stopScreen(uuid: UUID, name: String)
        = svc.screens_ss.stopScreen(uuid, name)

    /**
     * Stops any screen that the player is currently in
     *
     * @param uuid The player's UUID
     */
    fun stopAnyScreen(uuid: UUID)
        = svc.screens_ss.stopAnyScreen(uuid)

    // chat messages

    /**
     * Gets the chat log of a player
     *
     * @param uuid The player's UUID
     * @param entries The number of messages to get
     */
    fun getPlayerChatLog(uuid: UUID, entries: Int)
        = svc.messaging_ss.getPlayerChatLog(uuid, entries)

    /**
     * Restores the chat of a player, sending the last [entries] messages
     *
     * @param uuid The player's UUID
     * @param entries The number of messages to restore
     */
    fun restorePlayerChat(uuid: UUID, entries: Int)
        = svc.messaging_ss.restorePlayerChat(uuid, entries)

    /**
     * Clears the chat of a player, sending a 100-lines-long blank text message
     *
     * @param uuid The player's UUID
     */
    fun clearChat(uuid: UUID)
        = svc.messaging_ss.clearChat(uuid)

    // sending messages

    /**
     * Sends a message to the global channel
     *
     * @param message The message to send
     */
    fun sendGlobalMessage(message: Component)
        = svc.messaging_ss.sendGlobalMessage(message)

    /**
     * Sends a message to the staff channel
     *
     * @param message The message to send
     */
    fun sendStaffMessage(message: Component)
        = svc.messaging_ss.sendStaffMessage(message)

    /**
     * Sends a message to a player's personal channel
     *
     * @param uuid The player's UUID
     * @param message The message to send
     * @param respectScreens Whether to respect the player's screen
     */
    fun sendPersonalMessage(uuid: UUID, message: Component, respectScreens: Boolean = true)
        = svc.messaging_ss.sendPersonalMessage(uuid, message, respectScreens)

    /**
     * Sends a message to a channel
     *
     * @param channelName The name of the channel
     * @param message The message to send
     */
    fun sendChannelMessage(channelName: String, message: Component)
        = svc.messaging_ss.sendChannelMessage(channelName, message)

    /**
     * Sends a message to a player without any channel
     *
     * @param uuid The player's UUID
     * @param message The message to send
     * @param respectScreens Whether to respect the player's screen
     */
    fun sendChannellessMessage(uuid: UUID, message: Component, respectScreens: Boolean = true)
        = svc.messaging_ss.sendChannellessMessage(uuid, message, respectScreens)
}
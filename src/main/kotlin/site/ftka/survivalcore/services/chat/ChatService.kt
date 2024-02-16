package site.ftka.survivalcore.services.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import site.ftka.survivalcore.services.chat.subservices.ChatService_ChannelsSubservice
import site.ftka.survivalcore.services.chat.subservices.ChatService_ScreensSubservice
import java.util.UUID

class ChatService(var plugin: MClass) {
    val logger = plugin.loggingInitless.getLog("Chat", Component.text("Chat").color(NamedTextColor.DARK_RED))
    val api = ChatAPI(this)

    /*
        ChatService

        This service is meant to fully control
        every player's chat.

        HOW TO BE USED:
        Ask for a channel. Let's say, "Staff" using
     */

    val channels_ss = ChatService_ChannelsSubservice(this, plugin)
    val screens_ss = ChatService_ScreensSubservice(this, plugin)

    fun init() {
        logger.log("Initializing...", LogLevel.LOW)
        channels_ss.createElementalChannels()
    }

    // 1. reset all chat data
    // 2. create elemental channels
    // 3. create all needed channels (player, clan, etc)
    fun restart() {
        logger.log("Restarting...", LogLevel.LOW)

        // 1.
        channels_ss.clearMaps()

        // 2.
        channels_ss.createElementalChannels()

        // 3. players
        for (player in plugin.server.onlinePlayers)
            channels_ss.registerChannel(player.uniqueId.toString(), true, player.uniqueId)
    }

    /**
     * This method is ONLY FOR INTERNAL USAGE.
     *
     * The correct way to communicate with a player,
     * is through the player's channel.
     *
     * Get the player's channel using the getPlayerChannel method
     */
    fun sendRawMessageToPlayer(uuid: UUID, message: Component) {
        // get online player
        val player = plugin.server.getPlayer(uuid) ?: return

        // taking screens in count
        if (screens_ss.playersInsideActiveScreens.keys.contains(uuid))
            screens_ss.sendMessageAfterScreen(uuid, message)
        else
            player.sendMessage(message)
    }
}
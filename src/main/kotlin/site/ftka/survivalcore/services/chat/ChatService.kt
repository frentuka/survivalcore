package site.ftka.survivalcore.services.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import site.ftka.survivalcore.services.ServicesFramework
import site.ftka.survivalcore.services.chat.commands.ChatService_ExitScreenCommand
import site.ftka.survivalcore.services.chat.listeners.ChatListener
import site.ftka.survivalcore.services.chat.subservices.ChatService_ChannelsSubservice
import site.ftka.survivalcore.services.chat.subservices.ChatService_MessagingSubservice
import site.ftka.survivalcore.services.chat.subservices.ChatService_ScreensSubservice
import java.util.UUID

/**
 * ChatService is a class that handles the chat system,
 * like allowing players to exit from screens
 * or allowing staff to control screens and channels.
 *
 * @param plugin The main plugin class.
 * @param servicesFwk The services' framework.
 */
class ChatService(var plugin: MClass, var servicesFwk: ServicesFramework) {
    internal val logger = plugin.loggingInitless.getLog("Chat", Component.text("Chat").color(NamedTextColor.DARK_GRAY))
    internal val config = plugin.essentialsFwk.configs.chatConfig()
    val api = ChatAPI(this)

    /*
        ChatService

        This service is meant to fully control
        every player's chat.
     */

    internal val channels_ss =  ChatService_ChannelsSubservice(this, plugin)
    internal val screens_ss  = ChatService_ScreensSubservice(this, plugin)
    internal val messaging_ss = ChatService_MessagingSubservice(this, plugin)

    private val chatListener = ChatListener(this, plugin)
    private val exitScreenCommand = ChatService_ExitScreenCommand(this)

    internal fun init() {
        logger.log("Initializing...", LogLevel.LOW)

        channels_ss.createElementalChannels()

        plugin.initListener(chatListener)
        plugin.propEventsInitless.registerListener(chatListener)

        plugin.getCommand("exitscreen")?.setExecutor(exitScreenCommand)
    }

    // 1. reset all chat data
    // 2. create elemental channels
    // 3. create all needed channels (player, clan, etc)
    internal fun restart() {
        logger.log("Restarting...", LogLevel.LOW)

        // 1.
        channels_ss.clearMaps()

        // 2.
        channels_ss.createElementalChannels()

        // 3. players
        for (player in plugin.server.onlinePlayers)
            channels_ss.registerChannel(player.uniqueId.toString(), true, player.uniqueId)
    }

    internal fun stop() {
        logger.log("Stopping...", LogLevel.LOW)
    }
}
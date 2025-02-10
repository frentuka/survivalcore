package site.ftka.survivalcore.essentials.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.EssentialsFramework
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import site.ftka.survivalcore.essentials.chat.commands.ChatService_BackCommand
import site.ftka.survivalcore.essentials.chat.commands.ChatService_ExitCommand
import site.ftka.survivalcore.essentials.chat.commands.ChatService_SCommand
import site.ftka.survivalcore.essentials.chat.listeners.ChatListener
import site.ftka.survivalcore.essentials.chat.subservices.ChatService_ChannelsSubservice
import site.ftka.survivalcore.essentials.chat.subservices.ChatService_MessagingSubservice
import site.ftka.survivalcore.essentials.chat.subservices.ChatService_ScreensSubservice

/**
 * ChatService is a class that handles the chat system,
 * like allowing players to exit from screens
 * or allowing staff to control screens and channels.
 *
 * @param plugin The main plugin class.
 * @param servicesFwk The services' framework.
 */
class ChatEssential(private val plugin: MClass, val essentialsFwk: EssentialsFramework) {
    internal val logger = plugin.loggingInitless.getLog("Chat", Component.text("Chat").color(NamedTextColor.DARK_GRAY))
    internal val config = plugin.essentialsFwk.configs.chatConfig()
    val api = ChatAPI(this)

    /*
        Screen standards. idk where to put it
     */
    val BACK_BUTTON_SCREEN_STANDARD = "<reset><gray><click:run_command:'/backscreen'><hover:show_text:'<gray>Go back'><-</hover></click>"
    val EXIT_SCREEN_LINE_STANDARD = " <reset><click:run_command:'/exitscreen'><red><hover:show_text:'<red>Exit screen'>exit</hover></red></click>"

    /*
        ChatService

        This service is meant to fully control
        every player's chat.
     */

    internal val channels_ss =  ChatService_ChannelsSubservice(this, plugin)
    internal val screens_ss  = ChatService_ScreensSubservice(this, plugin)
    internal val messaging_ss = ChatService_MessagingSubservice(this, plugin)

    private val chatListener = ChatListener(this, plugin)

    private val exitCommand = ChatService_ExitCommand(this)
    private val backCommand = ChatService_BackCommand(this)
    private val sCommand = ChatService_SCommand(this)

    internal fun init() {
        logger.log("Initializing...", LogLevel.LOW)

        channels_ss.createElementalChannels()

        plugin.initListener(chatListener)
        plugin.propEventsInitless.registerListener(chatListener)

        plugin.initListener(sCommand)
        plugin.getCommand("s")?.setExecutor(sCommand)

        plugin.getCommand("exitscreen")?.setExecutor(exitCommand)
        plugin.getCommand("backscreen")?.setExecutor(backCommand)
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
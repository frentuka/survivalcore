package site.ftka.survivalcore.apps.ChatManager

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.ChatManager.commands.ChatManager_ChatCommand

/**
 * ChatSystemApp is a class that handles the chat system,
 * like allowing staff to control screens and channels.
 *
 * @param plugin The main plugin class.
 */
class ChatManagerApp(private val plugin: MClass) {

    private val chatCommand = ChatManager_ChatCommand(plugin)

    fun init() {
        plugin.getCommand("chat")?.setExecutor(chatCommand)
    }

    fun restart() {
        // nothing to restart
    }

    fun stop() {
        // nothing to stop
    }
}
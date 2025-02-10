package site.ftka.survivalcore.essentials.chat.objects

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/**
 * A screen-page object.
 *
 * @param message The message to display on the screen.
 * @param process The process to modify the message on every screen refresh.
 * @param onChat The action to perform when the player sends a chat message.
 * @param onSCommand The action to perform when the player sends "/s " command
 * @param onSCommandTabComplete The action that returns the tab completions for the "/s " command
 *
 * @property onChat Provides List<String> of arguments and sender's Player object
 * @property onSCommand Provides List<String> of arguments and sender's Player object
 * @property onSCommandTabComplete Provides List<String> of previous TabComplete and sender's Player object
 */
data class ChatScreenPage(
    private val message: String,
    private val process: (String) -> Component = /* default */ { Component.text(it) },
    val onChat: ((String, Player) -> Unit)? = null,
    val onSCommand: ((List<String>, Player) -> Unit)? = null,
    val onSCommandTabComplete: ((List<String>, Player) -> List<String>)? = null) {

    /*
        A screens-pages system can be kinda powerful. It's just a prototype.
        The idea is:

        - A screen is a collection of pages
        - A page is two things: a message and a process
        - The process modifies the message on every screen refresh

        So, a single page can modify itself in real-time
     */

    private var lastProcessedMessage = Component.text("none") as Component
    fun getMessage(processIt: Boolean = true): Component {
        if (!processIt)
            return lastProcessedMessage
        else
            lastProcessedMessage = process(message)
            return lastProcessedMessage
    }

}
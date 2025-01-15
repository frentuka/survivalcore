package site.ftka.survivalcore.services.chat.objects

import net.kyori.adventure.text.Component

class ChatScreenPage(private val message: Component, private val process: (Component) -> Component?, chatListener: ((String) -> Unit)? = null) {

    /*
        A screens-pages system can be kinda powerful. It's just a prototype.
        The idea is:

        - A screen is a collection of pages
        - A page is two things: a message and a process
        - The process modifies the message on every screen refresh

        So, a single page can modify itself in real-time
     */

    fun getMessage(): Component {
        return process(message) ?: message
    }

}
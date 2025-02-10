package site.ftka.survivalcore.essentials.chat.events

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropEvent
import java.util.UUID

class ChatService_ChatEvent(val sender: UUID, val message: Component): PropEvent {

    override val name: String
        get() = "ChatEvent"
    override val async: Boolean
        get() = true
    override var cancelled: Boolean = false
        get() = field
        set(value) { field = value }

}
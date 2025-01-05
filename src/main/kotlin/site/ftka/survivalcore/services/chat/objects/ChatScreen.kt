package site.ftka.survivalcore.services.chat.objects

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.services.chat.ChatService
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

data class ChatScreen(val service: ChatService, val name: String, val updateRateMillis: Long) {

    var isActive = true

    var screenContent: Component? = null
    val members = mutableSetOf<UUID>()
}
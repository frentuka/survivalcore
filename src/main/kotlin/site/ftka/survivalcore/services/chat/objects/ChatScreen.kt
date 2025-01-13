package site.ftka.survivalcore.services.chat.objects

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.services.chat.ChatService
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

data class ChatScreen(val service: ChatService, val player: UUID, val name: String) {

    var isActive = true

    // 30 seconds
    var timeoutMillis: Long = 1000 * 30

    // <Name, Page>
    var screenContent = mutableMapOf<String, ChatScreenPage>()

    var currentPage: String? = null

    fun getFrame(): Component? = screenContent[currentPage]?.getMessage()
}
package site.ftka.survivalcore.services.chat.subservices

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chat.ChatService
import site.ftka.survivalcore.services.chat.objects.ChatScreen
import java.util.*
import java.util.concurrent.TimeUnit

class ChatService_ScreensSubservice(private val service: ChatService, private val plugin: MClass) {

    val activeScreens = mutableMapOf<String, ChatScreen>()
    val playersInsideActiveScreens = mutableMapOf<UUID, String>()

    private val delayedPlayerMessages = mutableMapOf<UUID, MutableList<Component>>()
    fun sendMessageAfterScreen(uuid: UUID, message: Component) {
        if (delayedPlayerMessages.containsKey(uuid)) delayedPlayerMessages[uuid]?.add(message)
        else delayedPlayerMessages[uuid] = mutableListOf(message)
        delayMessageAfterScreen_scheduler()
    }

    private var dMASscheduler_isStarted = false
    private fun delayMessageAfterScreen_scheduler() {
        if (dMASscheduler_isStarted) return else dMASscheduler_isStarted = true
        plugin.globalScheduler.scheduleAtFixedRate(
            {

                for (keyval in delayedPlayerMessages) {
                    if (!playersInsideActiveScreens.containsKey(keyval.key)) {
                        for (message in delayedPlayerMessages[keyval.key]!!) {
                            service.sendRawMessageToPlayer(keyval.key, message)
                        }

                        delayedPlayerMessages.remove(keyval.key)
                    }
                }

            }
            , 0, 1, TimeUnit.SECONDS)
    }

}
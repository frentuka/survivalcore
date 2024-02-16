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

    var terminate = Runnable {
        service.screens_ss.activeScreens.remove(name)
        for (member in members) {
            if (service.screens_ss.playersInsideActiveScreens[member] == name)
                service.screens_ss.playersInsideActiveScreens.remove(member)
        }
    }

    private val screenScheduler = Executors.newSingleThreadScheduledExecutor()
    init {
        screenScheduler.scheduleAtFixedRate({

            if (!service.screens_ss.activeScreens.containsKey(name) || !isActive) {
                screenScheduler.shutdown() // shut this down
                service.screens_ss.activeScreens.remove(name) // remove from active screens
                for (member in members) {
                    service.screens_ss.playersInsideActiveScreens.remove(member)
                }

                for (keyval in service.screens_ss.playersInsideActiveScreens) // if there's a player inside this screen, remove it
                    if (keyval.value == name) service.screens_ss.playersInsideActiveScreens.remove(keyval.key)

                return@scheduleAtFixedRate
            }

            if (screenContent == null) return@scheduleAtFixedRate

            screenContent?.let {
                for (member in members) {
                    service.sendRawMessageToPlayer(member, it)
                }
            }

        }, 1000, updateRateMillis, TimeUnit.MILLISECONDS)
    }
}
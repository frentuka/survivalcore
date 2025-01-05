package site.ftka.survivalcore.services.chat.subservices

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chat.ChatService
import site.ftka.survivalcore.services.chat.objects.ChatScreen
import java.util.*
import java.util.concurrent.TimeUnit

class ChatService_ScreensSubservice(private val service: ChatService, private val plugin: MClass) {

    //                                                   <Player's UUID, Screen>
    private val playersInsideActiveScreens = mutableMapOf<UUID, ChatScreen>()

    fun showScreen(uuid: UUID, screen: ChatScreen): Boolean {
        // is player inside another screen?
        if (playersInsideActiveScreens.containsKey(uuid))
            return false

        // set screen to player
        playersInsideActiveScreens[uuid] = screen



        return false
    }

    // stop showing screen
    // name is not needed but I prefer to know that I'm stopping the right screen
    fun stopScreen(uuid: UUID, name: String) {
        if (!playersInsideActiveScreens.containsKey(uuid))
            return

        if (playersInsideActiveScreens[uuid]!!.name != name)
            return

        //playersInsideActiveScreens[uuid]?.terminate

        playersInsideActiveScreens.remove(uuid)
    }
}
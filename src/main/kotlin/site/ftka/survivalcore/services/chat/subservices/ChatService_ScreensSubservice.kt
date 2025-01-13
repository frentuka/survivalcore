package site.ftka.survivalcore.services.chat.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chat.ChatService
import site.ftka.survivalcore.services.chat.objects.ChatScreen
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ChatService_ScreensSubservice(private val svc: ChatService, private val plugin: MClass) {

    // <Player's UUID, Screen>
    private val playersInsideScreens = mutableMapOf<UUID, ChatScreen>()

    fun showScreen(uuid: UUID, screen: ChatScreen): Boolean {
        // is player inside another screen?
        if (playersInsideScreens.containsKey(uuid))
            return false

        updateScreenTimeout(uuid)

        // clear player's screen
        svc.messaging_ss.clearChat(uuid)

        // set screen to player
        playersInsideScreens[uuid] = screen

        // show screen's first frame
        showActiveFrame(uuid)

        return true
    }

    // stop showing screen
    // name is not needed but I prefer to know that I'm stopping the right screen
    fun stopScreen(uuid: UUID, name: String) {
        if (!playersInsideScreens.containsKey(uuid))
            return

        // other screen is active
        if (playersInsideScreens[uuid]?.name != name)
            return

        playersInsideScreens.remove(uuid)
        screenTimeoutLastUpdateMap.remove(uuid)

        // send player's corresponding chat
        svc.messaging_ss.restorePlayerChat(uuid, 50)
    }

    // refresh frame for player's active screen
    private fun showActiveFrame(uuid: UUID) {
        if (!playersInsideScreens.containsKey(uuid))
            return

        val screen = playersInsideScreens[uuid]!!
        screen.getFrame()?.let {
            svc.messaging_ss.clearChat(uuid)
            svc.messaging_ss.sendChannellessMessage(uuid, it, false)
        } ?: stopScreen(uuid, screen.name)
    }

    /*
        get
     */

    fun isPlayerInsideScreen(uuid: UUID) =
        playersInsideScreens.containsKey(uuid)

    /*
        screen page refresh & timeout check
     */

    private fun updateScreenTimeout(uuid: UUID) {
        screenTimeoutLastUpdateMap[uuid] = System.currentTimeMillis()
    }

    private var screensScheduledFuture: ScheduledFuture<*>? = null
    private val screenTimeoutLastUpdateMap = mutableMapOf<UUID, Long>()

    private fun serviceTimer() {
        if (screensScheduledFuture != null)
            return

        screensScheduledFuture = plugin.globalScheduler.scheduleAtFixedRate(
            {
                for (player in playersInsideScreens.keys) {
                    val screen = playersInsideScreens[player] ?: continue
                    var stopped = false

                    // if screen is not active, remove it
                    if (!screen.isActive) {
                        stopScreen(player, screen.name)
                        stopped = true
                    }

                    val currentTimeMillis = System.currentTimeMillis()
                    // if screen has timed out, remove it
                    if (!stopped && screenTimeoutLastUpdateMap[player]?.let { currentTimeMillis - it > screen.timeoutMillis } == true) {
                        stopScreen(player, screen.name)
                        stopped = true
                    }

                    // refresh screen
                    if (!stopped)
                        showActiveFrame(player)
                }
            }, 1, 1, TimeUnit.SECONDS
        )
    }
}
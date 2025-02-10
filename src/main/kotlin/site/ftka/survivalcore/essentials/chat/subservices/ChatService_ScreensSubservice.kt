package site.ftka.survivalcore.essentials.chat.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless
import site.ftka.survivalcore.essentials.chat.ChatEssential
import site.ftka.survivalcore.essentials.chat.objects.ChatScreen
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

internal class ChatService_ScreensSubservice(private val svc: ChatEssential, private val plugin: MClass) {
    private val logger = svc.logger.sub("Screens")

    // <Player's UUID, Screen>
    private val playersInsideScreens = mutableMapOf<UUID, ChatScreen>()

    fun showScreen(uuid: UUID, screen: ChatScreen): Boolean {
        // is player inside another screen?
        if (playersInsideScreens.containsKey(uuid))
            return false

        logger.log("Starting screen for $uuid", LoggingInitless.LogLevel.HIGH)

        // clear player's screen
        svc.messaging_ss.clearChat(uuid)

        // set screen to player
        playersInsideScreens[uuid] = screen

        // show screen's first frame
        sendActiveFrame(uuid)

        updateScreenTimeout(uuid)

        // activate service's timer
        serviceTimer()

        return true
    }

    fun refreshScreen(uuid: UUID, screen: String, page: String) {
        if (!playersInsideScreens.containsKey(uuid))
            return

        // safe-check
        if (playersInsideScreens[uuid]?.name != screen)
            return

        screenTimeoutLastUpdateMap[uuid] = System.currentTimeMillis()

        playersInsideScreens[uuid]?.currentPage = page
        sendActiveFrame(uuid)
    }

    // String is just a safe-check to check that the screen is the right one
    fun modifyScreen(uuid: UUID, name: String, modification: (ChatScreen) -> Unit) {
        if (!playersInsideScreens.containsKey(uuid))
            return

        if (!playersInsideScreens[uuid]?.name.equals(name))
            return

        modification(playersInsideScreens[uuid]!!)
    }

    // stop showing screen
    // name is not needed but I prefer to know that I'm stopping the right screen
    fun stopScreen(uuid: UUID, name: String) {
        if (!playersInsideScreens.containsKey(uuid))
            return

        // is this the right screen?
        if (playersInsideScreens[uuid]?.name != name)
            return

        stopAnyScreen(uuid)
    }

    fun stopAnyScreen(uuid: UUID) {
        playersInsideScreens.remove(uuid)
        screenTimeoutLastUpdateMap.remove(uuid)

        // send player's corresponding chat
        svc.messaging_ss.clearChat(uuid)
        svc.messaging_ss.restorePlayerChat(uuid, 50)
    }

    // refresh frame for player's active screen
    private fun sendActiveFrame(uuid: UUID) {
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

    internal fun isPlayerInsideScreen(uuid: UUID) =
        playersInsideScreens.containsKey(uuid)

    internal fun getActiveScreen(uuid: UUID): ChatScreen? =
        playersInsideScreens[uuid]

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
                    var stopped = !screen.isActive

                    // if screen is not active, remove it
                    if (stopped)
                        stopScreen(player, screen.name)

                    // if screen has timed out, remove it
                    val currentTimeMillis = System.currentTimeMillis()
                    val elapsedTimeMillis = currentTimeMillis - (screenTimeoutLastUpdateMap[player] ?: 0)
                    if (!stopped && screen.timeoutMillis >= 0 && screenTimeoutLastUpdateMap[player]?.let { elapsedTimeMillis > screen.timeoutMillis } == true) {
                        stopScreen(player, screen.name)
                        stopped = true
                    }

                    // refresh screen
                    if (!stopped)
                        sendActiveFrame(player)
                }
            }, 1, 1, TimeUnit.SECONDS
        )
    }
}
package site.ftka.survivalcore.essentials.actionbar

import net.kyori.adventure.text.Component
import java.util.UUID

class ActionBarAPI(private val ess: ActionBarEssential) {

    /**
     * Sends an action bar message to the player.
     *
     * @param uuid The player's UUID.
     * @param message The message to send.
     */
    fun sendActionBar(uuid: UUID, message: Component) {
        ess.messaging_ss.sendActionBar(uuid, message)
    }

}
package site.ftka.survivalcore.essentials.actionbar.subservices

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.actionbar.ActionBarEssential
import java.util.UUID

class ActionBarEssential_MessagingSubservice(private val plugin: MClass, private val ess: ActionBarEssential) {

    fun sendActionBar(uuid: UUID, message: Component) {
        val player = plugin.server.getPlayer(uuid) ?: return
        player.sendActionBar(message)
    }

}
package site.ftka.survivalcore.essentials.actionbar

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.EssentialsFramework
import site.ftka.survivalcore.essentials.actionbar.subservices.ActionBarEssential_MessagingSubservice

class ActionBarEssential(private val plugin: MClass, private val essFwk: EssentialsFramework) {

    internal val logger = plugin.loggingInitless.getLog("ActionBarEssential", Component.text("ActionBar"))
    internal val api = ActionBarAPI(this)

    internal val messaging_ss = ActionBarEssential_MessagingSubservice(plugin, this)

    internal fun init() {
        logger.log("Initializing...")
    }

    internal fun restart() {
        logger.log("Restarting...")
    }

    internal fun stop() {
        logger.log("Stopping...")
    }

}
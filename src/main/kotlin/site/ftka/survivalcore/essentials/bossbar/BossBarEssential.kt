package site.ftka.survivalcore.essentials.bossbar

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.EssentialsFramework
import site.ftka.survivalcore.essentials.bossbar.listeners.BossBarListener
import site.ftka.survivalcore.essentials.bossbar.subservices.BossBarEssential_MessagingSubservice

class BossBarEssential(private val plugin: MClass, private val essFwk: EssentialsFramework) {

    internal val logger = plugin.loggingInitless.getLog("BossBarEssential", Component.text("BossBar"))
    val api = BossBarAPI(this)

    internal val messaging_ss = BossBarEssential_MessagingSubservice(plugin, this)
    private val listener = BossBarListener(plugin, this)

    internal fun init() {
        logger.log("Initializing...")
        messaging_ss.init()
        plugin.initListener(listener)
    }

    internal fun restart() {
        logger.log("Restarting...")
        messaging_ss.restart()
    }

    internal fun stop() {
        logger.log("Stopping...")
        messaging_ss.stop()
    }

}

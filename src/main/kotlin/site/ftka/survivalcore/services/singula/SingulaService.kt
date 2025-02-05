package site.ftka.survivalcore.services.singula

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.ServicesFramework

/**
 * Will be in charge to provide and manage
 * all Singula and OfflineSingula objects
 */
class SingulaService(private val plugin: MClass, private val fwk: ServicesFramework) {

    internal val logger = plugin.loggingInitless.getLog("SingulaService", Component.text("Singula").color(NamedTextColor.LIGHT_PURPLE))
    val api = SingulaAPI(plugin, this)

    internal fun init() {
        logger.log("Initializing service...")
    }

    internal fun restart() {
        logger.log("Restarting service...")
    }

    internal fun stop() {
        logger.log("Stopping service...")
    }

}
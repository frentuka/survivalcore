package site.ftka.survivalcore.apps

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.PermissionsManager.PermissionsManagerApp
import site.ftka.survivalcore.apps.ServerAdministration.ServerAdministrationApp
import site.ftka.survivalcore.apps.WorldManager.WorldManagerApp

class AppsFramework(private val plugin: MClass) {
    internal val logger = plugin.loggingInitless.getLog(
        "AppsFramework",
        Component.text("Apps").color(NamedTextColor.BLACK)
    )

    val serverAdministration = ServerAdministrationApp(plugin)
    val permissionsManager = PermissionsManagerApp(plugin)
    val worldManager = WorldManagerApp(plugin)

    fun initAll() {
        logger.log("Initializing apps...")

        permissionsManager.init()
        serverAdministration.init()
        worldManager.init()
    }

    fun restartAll() {
        logger.log("Restarting apps...")

        permissionsManager.restart()
        serverAdministration.restart()
        worldManager.restart()
    }

    fun stopAll() {
        logger.log("Stopping apps...")

        permissionsManager.stop()
        serverAdministration.stop()
        worldManager.stop()
    }

}
package site.ftka.survivalcore.apps

import site.ftka.survivalcore.apps.ServerAdministration.ServerAdministrationApp
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.ChatManager.ChatManagerApp
import site.ftka.survivalcore.apps.PermissionsManager.PermissionsManagerApp

class AppsFramework(private val plugin: MClass) {
    private val logger = plugin.loggingInitless.getLog("AppsFramework", Component.text("Apps").color(NamedTextColor.BLACK))

    val serverAdministration = ServerAdministrationApp(plugin)
    val permissionsManager = PermissionsManagerApp(plugin)
    val chatManager = ChatManagerApp(plugin)

    fun initAll() {
        logger.log("Initializing apps...")

        chatManager.init()
        permissionsManager.init()
        serverAdministration.init()
    }

    fun restartAll() {
        logger.log("Restarting apps...")

        chatManager.restart()
        permissionsManager.restart()
        serverAdministration.restart()
    }

    fun stopAll() {
        logger.log("Stopping apps...")

        chatManager.stop()
        permissionsManager.stop()
        serverAdministration.stop()
    }

}
package site.ftka.survivalcore.apps

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.ServerAdministration.ServerAdministrationApp

class AppsFramework(private val plugin: MClass) {
    private val logger = plugin.loggingInitless.getLog("AppsFramework", Component.text("Apps").color(NamedTextColor.RED))

    val serverAdministration = ServerAdministrationApp(plugin)

    fun initAll() {
        logger.log("Initializing apps...")

        serverAdministration.init()
    }

    fun restartAll() {
        logger.log("Restarting apps...")

        serverAdministration.restart()
    }



}
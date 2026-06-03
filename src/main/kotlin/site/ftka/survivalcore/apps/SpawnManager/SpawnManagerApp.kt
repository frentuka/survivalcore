package site.ftka.survivalcore.apps.SpawnManager

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import site.ftka.survivalcore.apps.SpawnManager.commands.SpawnManagerCommand

class SpawnManagerApp(private val plugin: MClass) {
    internal val logger = plugin.loggingInitless.getLog("SpawnManager", Component.text("SpawnManager").color(NamedTextColor.YELLOW))

    internal fun init() {
        logger.log("Initializing...", LogLevel.LOW)
        val cmd = plugin.server.getPluginCommand("spawnmanager")
        if (cmd != null) {
            val executor = SpawnManagerCommand(plugin, this)
            cmd.setExecutor(executor)
            cmd.setTabCompleter(executor)
        }
    }

    internal fun restart() {
        logger.log("Restarting...", LogLevel.LOW)
        stop()
        init()
    }

    internal fun stop() {
        logger.log("Stopping...", LogLevel.LOW)
    }
}

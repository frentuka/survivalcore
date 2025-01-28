package site.ftka.survivalcore.essentials

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.configs.ConfigsEssential
import site.ftka.survivalcore.essentials.database.DatabaseEssential
import site.ftka.survivalcore.essentials.usernameTracker.UsernameTrackerEssential

internal class EssentialsFramework(private val plugin: MClass) {
    private val logger = plugin.loggingInitless.getLog("EssentialsFramework", Component.text("Essentials").color(NamedTextColor.RED))

    val configs         = ConfigsEssential(plugin)
    val database        = DatabaseEssential(plugin)
    val usernameTracker = UsernameTrackerEssential(this, plugin)

    fun initAll() {
        logger.log("Initializing essentials...")

        configs.init()
        database.init()
        usernameTracker.init()
    }

    fun restartAll() {
        logger.log("Restarting essentials...")

        configs.restart()
        database.restart()
        usernameTracker.restart()
    }

    fun stopAll() {
        logger.log("Stopping essentials...")

        configs.stop()
        database.stop()
        usernameTracker.stop()
    }

}
package site.ftka.survivalcore.essentials

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.configs.ConfigsEssential
import site.ftka.survivalcore.essentials.database.DatabaseEssential

class EssentialsFramework(private val plugin: MClass) {
    private val logger = plugin.loggingInitless.getLog("EssentialsFramework", Component.text("Essentials").color(NamedTextColor.RED))

    val configsEssential = ConfigsEssential(plugin)
    val dbEssential = DatabaseEssential(plugin)

    fun initAll() {
        logger.log("Initializing essentials...")

        configsEssential.init()
        dbEssential.init()
    }

    fun restartAll() {
        logger.log("Restarting essentials...")

        configsEssential.restart()
        dbEssential.restart()
    }

}
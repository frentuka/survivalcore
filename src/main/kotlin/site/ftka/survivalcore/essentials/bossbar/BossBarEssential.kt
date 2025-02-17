package site.ftka.survivalcore.essentials.bossbar

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.EssentialsFramework

class BossBarEssential(private val plugin: MClass, private val essFwk: EssentialsFramework) {
    internal val logger = plugin.loggingInitless.getLog("BossBarEssential", Component.text("BossBar").color(NamedTextColor.GOLD))

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
package site.ftka.survivalcore.services.gameplay

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import site.ftka.survivalcore.services.ServicesFramework
import site.ftka.survivalcore.services.gameplay.subservices.Gameplay_EntityFreezeSubservice
import site.ftka.survivalcore.services.gameplay.subservices.Gameplay_FirstJoinSubservice
import site.ftka.survivalcore.services.gameplay.subservices.Gameplay_MobSpawningSubservice
import site.ftka.survivalcore.services.gameplay.subservices.Gameplay_TerritoryBorderSubservice

class GameplayService(private val plugin: MClass, private val services: ServicesFramework) {
    internal val logger = plugin.loggingInitless.getLog("Gameplay", Component.text("Gameplay").color(NamedTextColor.YELLOW))

    internal val firstJoin_ss = Gameplay_FirstJoinSubservice(this, plugin)
    internal val mobSpawning_ss = Gameplay_MobSpawningSubservice(this, plugin)
    internal val entityFreeze_ss = Gameplay_EntityFreezeSubservice(this, plugin)
    internal val border_ss = Gameplay_TerritoryBorderSubservice(this, plugin)

    var isRestarting = false

    internal fun init() {
        logger.log("Initializing GameplayService...", LogLevel.LOW)
        firstJoin_ss.init()
        mobSpawning_ss.init()
        entityFreeze_ss.init()
        border_ss.init()
    }

    internal fun restart() {
        logger.log("Restarting GameplayService...", LogLevel.LOW)
        isRestarting = true
        
        firstJoin_ss.restart()
        mobSpawning_ss.restart()
        entityFreeze_ss.restart()
        border_ss.restart()
        
        isRestarting = false
    }

    internal fun stop() {
        logger.log("Stopping GameplayService...", LogLevel.LOW)
        firstJoin_ss.stop()
        mobSpawning_ss.stop()
        entityFreeze_ss.stop()
        border_ss.stop()
    }
}

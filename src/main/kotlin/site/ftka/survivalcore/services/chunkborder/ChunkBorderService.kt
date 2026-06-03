package site.ftka.survivalcore.services.chunkborder

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import site.ftka.survivalcore.services.ServicesFramework
import site.ftka.survivalcore.services.chunkborder.subservices.Border_GenerationSubservice
import site.ftka.survivalcore.services.chunkborder.subservices.Border_RestorationSubservice
import site.ftka.survivalcore.services.chunkborder.subservices.Border_StorageSubservice

class ChunkBorderService(private val plugin: MClass, private val services: ServicesFramework) {
    internal val logger = plugin.loggingInitless.getLog("ChunkBorder", Component.text("ChunkBorder").color(NamedTextColor.GRAY))

    val api = ChunkBorderAPI(this)

    internal val generation_ss = Border_GenerationSubservice(this, plugin)
    internal val storage_ss = Border_StorageSubservice(this, plugin)
    internal val restoration_ss = Border_RestorationSubservice(this, plugin)

    var isRestarting = false

    internal fun init() {
        logger.log("Initializing...", LogLevel.LOW)
        
        plugin.initListener(site.ftka.survivalcore.services.chunkborder.listeners.BorderListener(this, plugin))
    }

    internal fun restart() {
        logger.log("Restarting...", LogLevel.LOW)
        isRestarting = true

        isRestarting = false
    }

    internal fun stop() {
        logger.log("Stopping...", LogLevel.LOW)
        isRestarting = true
    }
}

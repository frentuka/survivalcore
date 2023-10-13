package site.ftka.survivalcore.services.playerdata.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.logging.LoggingEssential
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRegisterEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataUnregisterEvent
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*

class PlayerDataRegistrationSubservice(private val service: PlayerDataService, private val plugin: MClass) {
    val logger = service.logger
    val services = plugin.services

    // Register functions
    // 1. Obtain or create player's information
    // 2. Store in cache
    // 3. Report event
    fun register(uuid: UUID) {
        var playerdata: PlayerData? = null
        logger.log("Starting registration for uuid ($uuid)", LoggingEssential.LogLevel.DEBUG)

        // Check if dump exists.
        val dumpData = service.emergency_ss.checkDumpExists(uuid, true)

        if (dumpData != null) {
            logger.log("Detected emergency dump for uuid $uuid")

            playerdata = dumpData
            // Overwrite in database
            service.output_ss.asyncSet(uuid, playerdata)
        }
        // 1.
        else {
            services.dbService.asyncExists(uuid.toString()).whenCompleteAsync { existsResult, _ ->
                // If existsResult, then get
                // If not, then create
                if (!existsResult) {
                    logger.log("Creating new playerdata ($uuid)")
                    playerdata = service.output_ss.create(uuid)
                    return@whenCompleteAsync
                }

                logger.log("Retrieving playerdata for uuid ($uuid)", LoggingEssential.LogLevel.HIGH)
                service.input_ss.asyncGet(uuid).whenComplete { getResult, _ -> playerdata = getResult }
            }
        }

        logger.log("Caching playerdata for uuid ($uuid)", LoggingEssential.LogLevel.DEBUG)

        // 2.
        service.playerDataMap[uuid] = playerdata ?: return

        logger.log("Successfully cached playerdata for ${playerdata!!.username} ($uuid)", LoggingEssential.LogLevel.DEBUG)

        // 3.
        plugin.server.pluginManager.callEvent(PlayerDataRegisterEvent(uuid, playerdata))
    }

    // 1. Save player's information in database
    // 2. Remove from cache
    // 3. Report event
    fun unregister(uuid: UUID) {
        val playerdata = service.playerDataMap[uuid] ?: return

        services.dbService.asyncPing().whenComplete { result, _ ->
            // 1.
            if (result) service.output_ss.asyncSet(uuid, playerdata)
            else service.emergency_ss.emergencyDump(playerdata)

            // 2.
            service.playerDataMap.remove(uuid)

            logger.log("Unregistered playerdata: ($uuid)", LoggingEssential.LogLevel.DEBUG)

            // 3.
            plugin.server.pluginManager.callEvent(PlayerDataUnregisterEvent(uuid, playerdata))
        }
    }
}
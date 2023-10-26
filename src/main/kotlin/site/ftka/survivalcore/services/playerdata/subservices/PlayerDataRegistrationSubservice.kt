package site.ftka.survivalcore.services.playerdata.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.logging.LoggingEssential
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRegisterEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataUnregisterEvent
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import site.ftka.survivalcore.utils.textUtils
import java.util.*

class PlayerDataRegistrationSubservice(private val service: PlayerDataService, private val plugin: MClass) {
    val logger = service.logger

    // Register functions
    // 1. Obtain or create player's information
    // 2. Store in cache
    // 3. Report event
    fun register(uuid: UUID) {
        var playerdata: PlayerData? = null
        logger.log("Starting registration for uuid ($uuid)", LoggingEssential.LogLevel.DEBUG)

        // 1.
        (plugin.dbEssential.asyncExists(uuid.toString()) ?: return).also {
            it.whenCompleteAsync { existsResult, _ ->
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
        (playerdata ?: return).also { service.playerDataMap[it.uuid] = it }

        logger.log("Successfully cached playerdata for ${playerdata!!.username} ($uuid)", LoggingEssential.LogLevel.DEBUG)

        // 3.
        plugin.server.pluginManager.callEvent(PlayerDataRegisterEvent(uuid, playerdata))
    }

    // 1. Save player's information in database
    // 2. Remove from cache
    // 3. Report event
    fun unregister(uuid: UUID) {
        val playerdata = service.playerDataMap[uuid] ?: return

        // 1.
        service.output_ss.asyncSet(playerdata) // IF SET FAILS, EMERGENCY DUMP IS DONE IN OUTPUT SUBSERVICE

        // 2.
        service.playerDataMap.remove(uuid)

        logger.log("Unregistered playerdata: ($uuid)", LoggingEssential.LogLevel.DEBUG)

        // 3.
        plugin.server.pluginManager.callEvent(PlayerDataUnregisterEvent(uuid, playerdata))
    }
}
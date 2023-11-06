package site.ftka.survivalcore.services.playerdata.subservices

import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.logging.LoggingEssential
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataPreUnregisterEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRegisterEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataUnregisterEvent
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*
import java.util.concurrent.CompletableFuture

class PlayerData_RegistrationSubservice(private val service: PlayerDataService, private val plugin: MClass) {
    val logger = service.logger

    // Register functions
    // 1. Obtain or create player's information (done in this function)
    // 2. Store in cache (in finishRegistration())
    // 3. Apply PlayerState
    // 4. Call PlayerDataRegistrationEvent (in finishRegistration())
    fun register(uuid: UUID, name: String, async: Boolean = true) {
        logger.log("Starting registration for uuid ($uuid)", LoggingEssential.LogLevel.DEBUG)

        // 1.
        val exists = if (async) service.input_ss.asyncExists(uuid) ?: return
        else CompletableFuture.completedFuture(service.input_ss.syncExists(uuid) ?: return)

        exists.whenComplete { existsResult, _ ->
            // If existsResult, then get
            // If not, then create
            if (!existsResult) {
                logger.log("Creating new playerdata ($uuid)")

                // Create and save
                val playerdata = PlayerData(uuid) // create object
                finishRegistration(playerdata)

            } else { // Exists in database!
                logger.log("Retrieving playerdata from database for uuid ($uuid)", LoggingEssential.LogLevel.HIGH)

                val get = if (async) service.input_ss.asyncGet(uuid) ?: return@whenComplete
                else CompletableFuture.completedFuture(service.input_ss.syncGet(uuid) ?: return@whenComplete)
                get.whenComplete { getResult, _ ->
                    if (getResult == null) {
                        logger.log(
                            "Creating new playerdata as database data seems corrupted ($uuid)",
                            LoggingEssential.LogLevel.HIGH
                        )
                        finishRegistration(PlayerData(uuid))
                    } else
                        finishRegistration(getResult)
                }
            }
        }
    }

    // Last 2 steps must be done in another function
    // because of async whenComplete
    private fun finishRegistration(playerdata: PlayerData) {
        // 2.
        service.playerDataMap[playerdata.uuid] = playerdata
        logger.log("Successfully cached playerdata for ${playerdata.info.username} (${playerdata.uuid})", LoggingEssential.LogLevel.DEBUG)

        // 4.
        plugin.server.pluginManager.callEvent(PlayerDataRegisterEvent(playerdata.uuid, playerdata))
    }



    // 1. Call PlayerDataPreUnregistrationEvent
    // 2. Save player's information in database (in finishUnregistration())
    // 3. Remove from cache (in finishUnregistration())
    // 4. Report PlayerDataUnregistrationEvent (in finishUnregistration())
    fun unregister(uuid: UUID, player: Player? = null, async: Boolean = true) {
        if (service.playerDataMap[uuid] == null ) {
            logger.log("Tried to unregister ($uuid) but no playerdata was found", LoggingEssential.LogLevel.DEBUG)
            return
        }
        val playerdata = service.playerDataMap[uuid]!!

        logger.log("Pre-unregistering playerdata: ($uuid)", LoggingEssential.LogLevel.DEBUG)

        player?.let { plugin.server.pluginManager.callEvent(PlayerDataPreUnregisterEvent(uuid, playerdata, it)) }

        logger.log("Unregistering playerdata: ($uuid)", LoggingEssential.LogLevel.DEBUG)

        finishUnregistration(playerdata, async)
    }

    private fun finishUnregistration(playerdata: PlayerData, async: Boolean) {
        val uuid = playerdata.uuid

        // 1.
        service.output_ss.asyncSet(playerdata) // IF SET FAILS, EMERGENCY DUMP IS DONE IN OUTPUT SUBSERVICE

        logger.log("Setted in database: ($uuid)", LoggingEssential.LogLevel.DEBUG)

        // 2.
        service.playerDataMap.remove(uuid)

        logger.log("Calling unregister event: ($uuid)", LoggingEssential.LogLevel.DEBUG)

        // 3.
        plugin.server.pluginManager.callEvent(PlayerDataUnregisterEvent(uuid, playerdata))
    }

}
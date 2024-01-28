package site.ftka.survivalcore.services.playerdata.subservices

import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.logging.LoggingEssential
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRegisterEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataUnregisterEvent
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*

class PlayerData_RegistrationSubservice(private val service: PlayerDataService, private val plugin: MClass) {
    private val logger = service.logger.sub("Registration")

    // Register functions
    // 1. Obtain or create player's information (done in this function)
    // 2. Apply appliable modules
    // 3. Store in cache (in finishRegistration())
    // 4. Call PlayerDataRegistrationEvent (in finishRegistration())
    fun register(uuid: UUID, player: Player? = null, async: Boolean = true) {
        logger.log("Starting registration for uuid ($uuid)", LoggingEssential.LogLevel.DEBUG)

        // 1.
        val exists = service.input_ss.exists(uuid, async) ?: return

        exists.whenComplete { existsResult, exc ->
            if (exc != null) {
                logger.log("An exception occurred when retrieving data for ($uuid). Kicking player.")
                player?.kick()
                exc.printStackTrace()
            }

            // If existsResult, then get
            // If not, then create
            if (!existsResult) {

                logger.log("Creating new playerdata ($uuid)")

                // Create and save
                val playerdata = PlayerData(uuid) // create object
                finishRegistration(playerdata, player)

            } else { // Exists in database!

                logger.log("Retrieving playerdata from database ($uuid)", LoggingEssential.LogLevel.HIGH)
                gatherPlayerData(uuid, async, player)

            }
        }
    }

    private fun gatherPlayerData(uuid: UUID, async: Boolean, player: Player?) {
        val get = service.input_ss.get(uuid, async) ?: return
        get.whenComplete { getResult, exc ->

            if (exc != null) {
                logger.log("There was an exception when trying to gather data from database for $uuid. Kicking player")
                player?.kick()
                exc.printStackTrace()
                return@whenComplete
            }

            val playerdata = getResult ?: PlayerData(uuid)
            if (getResult == null)
                logger.log("Creating new playerdata as database data seems corrupted ($uuid)", LoggingEssential.LogLevel.LOW)

            finishRegistration(playerdata, player)
        }
    }

    private fun finishRegistration(playerdata: PlayerData, player: Player? = null) {
        // 2.
        player?.let {
            // PlayerInformation
            playerdata.info.updateValuesFromPlayer(it)

            // PlayerState
            playerdata.state.applyValuesToPlayer(plugin, it)
        }

        // 3.
        service.playerDataMap[playerdata.uuid] = playerdata
        logger.log("Successfully cached playerdata for ${playerdata.info.username} (${playerdata.uuid})", LoggingEssential.LogLevel.DEBUG)

        // 4.
        plugin.eventsEssential.fireEvent(PlayerDataRegisterEvent(playerdata.uuid, playerdata))
    }





    // 1. Gather PlayerState
    // 2. Save player's information in database (in finishUnregistration())
    // 3. Remove from cache (in finishUnregistration())
    // 4. Report PlayerDataUnregistrationEvent (in finishUnregistration())
    fun unregister(uuid: UUID, player: Player? = null, async: Boolean = true) {
        // debug

        for (key in service.playerDataMap)
            println(key.key.toString())

        // debug end
        if (service.playerDataMap[uuid] == null) {
            logger.log("Tried to unregister ($uuid) but no playerdata was found", LoggingEssential.LogLevel.DEBUG)
            return
        }
        val playerdata = service.playerDataMap[uuid]!!

        // 1.
        player?.let {
            logger.log("Player found. Gathering playerstate for ($uuid)", LoggingEssential.LogLevel.DEBUG)
            playerdata.state.gatherValuesFromPlayer(player)
            playerdata.info.updateValuesFromPlayer(player)
        }

        logger.log("Unregistering playerdata: ($uuid)", LoggingEssential.LogLevel.DEBUG)

        finishUnregistration(playerdata, async)
    }

    private fun finishUnregistration(playerdata: PlayerData, async: Boolean) {
        val uuid = playerdata.uuid

        // 1.
        service.output_ss.set(playerdata, async) // IF SET FAILS, EMERGENCY DUMP IS DONE IN OUTPUT SUBSERVICE

        logger.log("Setted in database: ($uuid)", LoggingEssential.LogLevel.DEBUG)

        // 2.
        service.playerDataMap.remove(uuid)

        logger.log("Calling unregister event: ($uuid)", LoggingEssential.LogLevel.DEBUG)

        // 3.
        plugin.eventsEssential.fireEvent(PlayerDataUnregisterEvent(uuid, playerdata))
    }

}
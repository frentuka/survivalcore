package site.ftka.survivalcore.services.playerdata.subservices

import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRegisterEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataUnregisterEvent
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import site.ftka.survivalcore.services.playerdata.objects.modules.PlayerInformation
import site.ftka.survivalcore.services.playerdata.objects.modules.PlayerPermissions
import site.ftka.survivalcore.services.playerdata.objects.modules.PlayerSettings
import site.ftka.survivalcore.services.playerdata.objects.modules.PlayerState
import java.util.*

class PlayerData_RegistrationSubservice(private val service: PlayerDataService, private val plugin: MClass) {
    private val logger = service.logger.sub("Registration")

    // Register functions
    // 1. Obtain or create player's information (done in this function)
    // 2. Apply appliable modules
    // 3. Store in cache (in finishRegistration())
    // 4. Call PlayerDataRegistrationEvent (in finishRegistration())
    fun register(uuid: UUID, player: Player? = null, async: Boolean = true) {
        logger.log("Starting registration for uuid ($uuid)", LoggingInitless.LogLevel.DEBUG)

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

                logger.log("Does not exist in database", LogLevel.HIGH)
                logger.log("Creating new playerdata ($uuid)")

                // Create and save
                val playerdata = PlayerData(uuid) // create object
                finishRegistration(playerdata, player, true)

            } else { // Exists in database!

                logger.log("PlayerData exists in database ($uuid)", LogLevel.HIGH)
                gatherPlayerData(uuid, async, player)

            }
        }
    }

    private fun gatherPlayerData(uuid: UUID, async: Boolean, player: Player?) {
        val get = service.input_ss.get(uuid, async)
        get?.whenComplete { getResult, exc ->

            if (exc != null) {
                logger.log("There was an exception when trying to gather data from database for $uuid. Kicking player", LogLevel.LOW, NamedTextColor.RED)
                player?.kick()
                exc.printStackTrace()
                return@whenComplete
            }

            val playerdata = getResult ?: PlayerData(uuid)
            if (getResult == null) {
                logger.log("Creating new playerdata as database data seems corrupted ($uuid)", LogLevel.LOW)
                service.backup_ss.backupFromRequestBuffer(uuid)
            }

            logger.log("Gathered playerdata from database: $getResult", LogLevel.DEBUG)
            finishRegistration(playerdata, player, false)
        }
    }

    private fun finishRegistration(playerdata: PlayerData, player: Player? = null, firstJoin: Boolean = false) {
        // 2.

        player?.let {
            // PlayerData's modules could be null because of wrong json parsing.
            // In this case, missing modules are replaced and a copy of database's response is backed up.
            if (playerdata.information == null
                || playerdata.state == null
                || playerdata.settings == null
                || playerdata.permissions == null) {
                logger.log("PlayerInformation is null. Creating new one. Backup will be saved.", LogLevel.LOW, NamedTextColor.RED)
                service.backup_ss.backupFromRequestBuffer(player.uniqueId)

                player.sendMessage(plugin.servicesFwk.language.defaultLanguagePack.playerdata_error_corruptedPlayerData)
            }

            if (playerdata.information == null) playerdata.information = PlayerInformation()
            if (playerdata.state == null) playerdata.state = PlayerState()
            if (playerdata.settings == null) playerdata.settings = PlayerSettings()
            if (playerdata.permissions == null) playerdata.permissions = PlayerPermissions()


            // Update some modules that need to be updated
            // PlayerInformation
            playerdata.information?.updateValuesFromPlayer(it)

            // PlayerState
            playerdata.state?.applyValuesToPlayer(plugin, it)
        }

        // 3.
        service.putPlayerDataMap(playerdata.uuid, playerdata)
        logger.log("Stored playerdata in memory for ${playerdata.information?.username} (${playerdata.uuid})", LogLevel.DEBUG)

        // 4.
        plugin.propEventsInitless.fireEvent(PlayerDataRegisterEvent(playerdata.uuid, playerdata, firstJoin))
    }





    // 1. Gather PlayerState
    // 2. Save player's information in database (in finishUnregistration())
    // 3. Remove from cache (in finishUnregistration())
    // 4. Report PlayerDataUnregistrationEvent (in finishUnregistration())
    fun unregister(uuid: UUID, player: Player? = null, async: Boolean = true) {
        // debug

        for (key in service.getPlayerDataMap())
            println(key.key.toString())

        // debug end
        if (service.getPlayerData(uuid) == null) {
            logger.log("Tried to unregister ($uuid) but no playerdata was found", LogLevel.LOW, NamedTextColor.RED)
            return
        }
        val playerdata = service.getPlayerData(uuid)!!

        // 1.
        player?.let {
            logger.log("Player found. Gathering playerstate for ($uuid)", LogLevel.DEBUG)
            playerdata.state?.gatherValuesFromPlayer(player)
            playerdata.information?.updateValuesFromPlayer(player)
        }

        logger.log("Unregistering playerdata: ($uuid)", LogLevel.DEBUG)

        finishUnregistration(playerdata, async)
    }

    private fun finishUnregistration(playerdata: PlayerData, async: Boolean) {
        val uuid = playerdata.uuid

        // 1.
        service.output_ss.set(playerdata, async) // IF SET FAILS, EMERGENCY DUMP IS DONE IN OUTPUT SUBSERVICE

        logger.log("Set in database: ($uuid)", LogLevel.DEBUG)

        // 2.
        service.removePlayerData(uuid)

        logger.log("Calling unregister event: ($uuid)", LogLevel.DEBUG)

        // 3.
        plugin.propEventsInitless.fireEvent(PlayerDataUnregisterEvent(uuid, playerdata))
    }

}
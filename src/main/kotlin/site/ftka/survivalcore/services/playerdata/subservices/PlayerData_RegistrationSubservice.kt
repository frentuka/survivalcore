package site.ftka.survivalcore.services.playerdata.subservices

import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
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

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock

internal class PlayerData_RegistrationSubservice(private val service: PlayerDataService, private val plugin: MClass) {
    private val logger = service.logger.sub("Registration")

    // Register functions
    // 1. Obtain or create player's information
    // 2. Apply applicable modules
    // 3. Store in cache (in finishRegistration())
    // 4. Call PlayerDataRegistrationEvent (in finishRegistration())
    @OptIn(DelicateCoroutinesApi::class)
    fun register(uuid: UUID, player: Player? = null) {
        GlobalScope.launch {
            val lock = service.data.getLock(uuid)
            lock.withLock {
                logger.log("Starting registration for uuid ($uuid)", LogLevel.DEBUG)

                // 1. Check if exists and get data
                // By combining these, we save a database trip
                val playerdata = try {
                    service.inout_ss.get(uuid, async = true)?.await() ?: PlayerData(uuid).also {
                        logger.log("Does not exist in database, creating new ($uuid)", LogLevel.HIGH)
                    }
                } catch (e: Exception) {
                    logger.log("An exception occurred when retrieving data for ($uuid). Kicking player.")
                    player?.kick()
                    e.printStackTrace()
                    return@withLock
                }

                logger.log("Gathered/Created playerdata for ($uuid)", LogLevel.DEBUG)

                // Update timestamp to mark this data as "active"
                playerdata.updateTimestamp = System.currentTimeMillis()

                finishRegistration(playerdata, player, firstJoin = playerdata.updateTimestamp == 0L)
            }
        }
    }

    private fun finishRegistration(playerdata: PlayerData, player: Player? = null, firstJoin: Boolean = false) {
        // 2.

        player?.let {
            // PlayerData's modules could be null because of wrong json parsing.
            // In this case, missing modules are replaced and a copy of database's response is backed up.
            if (!service.integrity_ss.checkIntegrity(playerdata)) {
                logger.log("PlayerData integrity check failed. Creating new one. Backup will be saved.", LogLevel.LOW, NamedTextColor.RED)
                service.backup_ss.backupFromRequestBuffer(player.uniqueId)

                player.sendMessage(plugin.servicesFwk.language.defaultLanguagePack.playerdata_error_corruptedPlayerData)

                if (playerdata.information == null) playerdata.information = PlayerInformation()
                if (playerdata.state == null) playerdata.state = PlayerState()
                if (playerdata.settings == null) playerdata.settings = PlayerSettings()
                if (playerdata.permissions == null) playerdata.permissions = PlayerPermissions()
            }

            // Update some modules that need to be updated
            // PlayerInformation
            playerdata.information?.updateValuesFromPlayer(it)

            // PlayerState
            playerdata.state?.applyValuesToPlayer(plugin, it)
        }

        // 3.
        service.data.putPlayerData(playerdata.uuid, playerdata)
        logger.log("Stored playerdata in memory for ${playerdata.information?.username} (${playerdata.uuid})", LogLevel.DEBUG)

        // 4.
        plugin.propEventsInitless.fireEvent(PlayerDataRegisterEvent(playerdata.uuid, playerdata, firstJoin))
    }


    // 1. Gather PlayerState
    // 2. Save player's information in database (in finishUnregistration())
    // 3. Remove from cache (in finishUnregistration())
    // 4. Report PlayerDataUnregistrationEvent (in finishUnregistration())
        @OptIn(DelicateCoroutinesApi::class)
    fun unregister(uuid: UUID, player: Player? = null) {
        GlobalScope.launch {
            val lock = service.data.getLock(uuid)
            lock.withLock {
                val playerdata = service.data.getPlayerData(uuid) ?: run {
                    logger.log("Tried to unregister ($uuid) but no playerdata was found", LogLevel.LOW, NamedTextColor.RED)
                    return@withLock
                }

                val safePlayer = player ?: plugin.server.getPlayer(uuid)

                safePlayer?.let {
                    logger.log("Player found. Gathering playerstate for ($uuid)", LogLevel.DEBUG)
                    playerdata.state?.gatherValuesFromPlayer(safePlayer)
                    playerdata.information?.updateValuesFromPlayer(safePlayer)
                }

        logger.log("Unregistering playerdata: ($uuid)", LogLevel.DEBUG)

                finishUnregistration(playerdata, async = true)
            }
            // Cleanup happens AFTER the lock is released
            service.data.cleanupLock(uuid)
        }
    }


    private fun finishUnregistration(playerdata: PlayerData, async: Boolean) {
        val uuid = playerdata.uuid

        // 1.
        // If set happens to fail, EmergencyDump will be triggered inside output_ss
        service.inout_ss.set(playerdata, async)

        logger.log("Set in database: ($uuid)", LogLevel.DEBUG)

        // 2.
        service.data.removePlayerData(uuid)

        logger.log("Calling unregister event: ($uuid)", LogLevel.DEBUG)

        // 3.
        plugin.propEventsInitless.fireEvent(PlayerDataUnregisterEvent(uuid, playerdata))
    }

}
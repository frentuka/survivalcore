package site.ftka.survivalcore.services.playerdata

import com.google.gson.Gson
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.logging.LoggingEssential.LogLevel
import site.ftka.survivalcore.essentials.logging.objects.ServiceLogger
import site.ftka.survivalcore.services.playerdata.listeners.PlayerDataListener
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import site.ftka.survivalcore.services.playerdata.subservices.*
import java.util.UUID

class PlayerDataService(private val plugin: MClass) {
    private val services = plugin.services
    val logger: ServiceLogger = plugin.loggingEssential.getLog("PlayerData", "&3[PDATA]")

    // subservices
    val input_ss = PlayerDataInputSubservice(this, plugin)
    val output_ss = PlayerDataOutputSubservice(this, plugin)
    val registration_ss = PlayerDataRegistrationSubservice(this, plugin)
    val update_ss = PlayerDataUpdateSubservice(this, plugin)
    val emergency_ss = PlayerDataEmergencySubservice(this, plugin)

    // playerDataMap saves player's playerdata
    // onlinePlayers (UUIDs -> Usernames) is controlled by PlayerDataListener
    val playerDataMap: MutableMap<UUID, PlayerData> = mutableMapOf()
    val onlinePlayers: MutableMap<UUID, String> = mutableMapOf()

    fun init() {
        logger.log("&eInitializing PlayerData service.", LogLevel.LOW)

        // initialize listeners
        plugin.server.pluginManager.registerEvents(PlayerDataListener(this, plugin), plugin)
    }

    // Save and re-gather every connected player's information
    // 1. Unregister every cached player
    // 2. If exist, send emergency dumps to database
    // 3. Register every online player
    fun restart() {
        logger.log("Restarting PlayerData service.", LogLevel.LOW)

        // unregister everyone
        for (playerdata in playerDataMap.values)
            registration_ss.unregister(playerdata.uuid)

        playerDataMap.clear()
        onlinePlayers.clear()

        if (!services.dbService.syncPing()) { // ABORT EVERYTHING!!!!!
            logger.log("CRITICAL ERROR. DATABASE PING FAILED. ABORTING.", LogLevel.LOW)
            return
        }

        // 2. (missing security stuff to prevent overwriting wrong data, will add later.)
        for (emergencyDump in emergency_ss.checkAvailableDumps()) {
            output_ss.asyncSet(emergencyDump.uuid, emergencyDump)
        }
        emergency_ss.flushEmergencyDumps()

        // 3.
        for (player in plugin.server.onlinePlayers) {
            onlinePlayers[player.uniqueId] = player.name
            registration_ss.register(player.uniqueId)
        }
    }

    fun fromJson(json: String?): PlayerData? = Gson().fromJson(json, PlayerData::class.java)
}
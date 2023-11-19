package site.ftka.survivalcore.services.playerdata

import com.google.gson.Gson
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.logging.LoggingEssential.LogLevel
import site.ftka.survivalcore.essentials.logging.objects.ServiceLogger
import site.ftka.survivalcore.services.ServicesCore
import site.ftka.survivalcore.services.playerdata.events.PlayerDataInitEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRestartEvent
import site.ftka.survivalcore.services.playerdata.listeners.PlayerDataListener
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import site.ftka.survivalcore.services.playerdata.subservices.*
import java.util.UUID

class PlayerDataService(private val plugin: MClass, private val services: ServicesCore) {
    val logger: ServiceLogger = plugin.loggingEssential.getLog("PlayerData", Component.text("PlayerData").color(NamedTextColor.DARK_AQUA))

    // Some things must not be done while service is restarting
    // like playerdata modifications or unregister
    var isRestarting = false

    // subservices
    val input_ss = PlayerData_InputSubservice(this, plugin)
    val output_ss = PlayerData_OutputSubservice(this, plugin)
    val registration_ss = PlayerData_RegistrationSubservice(this, plugin)
    val update_ss = PlayerData_UpdateSubservice(this, plugin)
    val emergency_ss = PlayerData_EmergencySubservice(this, plugin)

    // listeners
    val playerDataListener = PlayerDataListener(this, plugin)

    // playerDataMap saves player's playerdata
    // onlinePlayers (UUIDs -> Usernames) is controlled by PlayerDataListener
    val playerDataMap: MutableMap<UUID, PlayerData> = mutableMapOf()
    val onlinePlayers: MutableMap<UUID, String> = mutableMapOf()

    fun init() {
        logger.log("&eInitializing PlayerData service.", LogLevel.LOW)

        // initialize listeners
        plugin.initListener(playerDataListener)
        plugin.eventsEssential.registerListener(playerDataListener)

        // call event
        plugin.eventsEssential.fireEvent(PlayerDataInitEvent())
    }

    // Save and re-gather every connected player's information
    // 1. Unregister every cached player
    // 2. If exist, send emergency dumps to database
    // 3. Register every online player
    // 4. Call event
    fun restart() {
        isRestarting = true
        logger.log("Restarting PlayerData service.", LogLevel.LOW)

        // unregister everyone
        for (playerdata in playerDataMap.values) {
            // if player is online, including it will save it's playerstate before unregistering
            // this step MUST NOT be async.
            // IF DATABASE HEALTH FAILS,
            // EMERGENCY DUMP IS AUTOMATICALLY CREATED IN OUTPUT_SS
            val player = plugin.server.getPlayer(playerdata.uuid)
            registration_ss.unregister(playerdata.uuid, player, false)
        }

        // if database is not available
        if (!plugin.dbEssential.health) { // ABORT EVERYTHING!!!!!
            logger.log("CRITICAL ERROR. DATABASE PING FAILED. ABORTING.", LogLevel.LOW)
            plugin.server.shutdown()
            return
        }

        playerDataMap.clear()
        onlinePlayers.clear()

        // upload everyone (health check was done before)
        // if emergency dump is older than database dump, it will NOT be uploaded
        // MUST NOT be async
        emergency_ss.uploadAllDumpsToDatabase(false)

        // 3.
        for (player in plugin.server.onlinePlayers) {
            onlinePlayers[player.uniqueId] = player.name
            registration_ss.register(player.uniqueId, player)
        }

        isRestarting = false

        // 4.
        plugin.eventsEssential.fireEvent(PlayerDataRestartEvent())
    }

    fun fromJson(json: String?): PlayerData? = Gson().fromJson(json, PlayerData::class.java)
}
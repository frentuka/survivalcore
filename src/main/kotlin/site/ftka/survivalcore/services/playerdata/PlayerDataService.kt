package site.ftka.survivalcore.services.playerdata


import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import site.ftka.survivalcore.initless.logging.objects.ServiceLogger
import site.ftka.survivalcore.services.ServicesFramework
import site.ftka.survivalcore.services.playerdata.events.PlayerDataInitEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRestartEvent
import site.ftka.survivalcore.services.playerdata.listeners.PlayerDataListener
import site.ftka.survivalcore.services.playerdata.listeners.WorldPlayerDataDeleter
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import site.ftka.survivalcore.services.playerdata.subservices.*
import java.util.concurrent.TimeUnit

class PlayerDataService(private val plugin: MClass, private val services: ServicesFramework) {
    val logger: ServiceLogger =
        plugin.loggingInitless.getLog("PlayerData", Component.text("PlayerData").color(NamedTextColor.DARK_AQUA))

    val api = PlayerDataAPI(this)
    val data = PlayerDataServiceData(this, plugin)

    // subservices
    val inout_ss = PlayerData_InputOutputSubservice(this, plugin)
    val backup_ss = PlayerData_BackupSubservice(this, plugin)
    val integrity_ss = PlayerData_IntegritySubservice(this, plugin)
    val registration_ss = PlayerData_RegistrationSubservice(this, plugin)
    val emergency_ss = PlayerData_EmergencySubservice(this, plugin)
    val caching_ss = PlayerData_CachingSubservice(this, plugin)

    // listeners
    val playerDataListener = PlayerDataListener(this, plugin)

    // fast access vals
    private val essFwk = plugin.essentialsFwk
    val baseFolderPath = "/${plugin.dataFolder.absolutePath}/PlayerData"

    // Some things must not be done while service is restarting
    // like playerdata modifications or regs/unregs
    var isRestarting = false

    @OptIn(DelicateCoroutinesApi::class)
    fun init() {
        logger.log("Initializing...", LogLevel.LOW)

        // initialize listeners
        plugin.initListener(playerDataListener)
        plugin.propEventsInitless.registerListener(playerDataListener)

        plugin.initListener(WorldPlayerDataDeleter(plugin))

        // call event after 1 second
        GlobalScope.launch {
            TimeUnit.SECONDS.sleep(1)
            plugin.propEventsInitless.fireEvent(PlayerDataInitEvent())
        }
    }

    // Save and re-gather every connected player's information
    // 1. Unregister every cached player
    // 2. If exists, send emergency dumps to database
    // 3. Register every online player
    // 4. Call event
    fun restart() {
        logger.log("Restarting...", LogLevel.LOW)
        isRestarting = true

        // unregister everyone
        for (playerdata in data.getPlayerDataMap().values) {
            // if player is online, including it will save it's playerstate before unregistering
            // this step MUST NOT be async.
            // IF DATABASE HEALTH FAILS,
            // EMERGENCY DUMP IS AUTOMATICALLY CREATED IN OUTPUT_SS
            val player = plugin.server.getPlayer(playerdata.uuid)
            registration_ss.unregister(playerdata.uuid, player, false)
        }

        // if database is not available
        if (!essFwk.database.health) { // ABORT EVERYTHING!!!!!
            logger.log("CRITICAL ERROR. DATABASE PING FAILED. ABORTING.", LogLevel.LOW, NamedTextColor.RED)
            plugin.server.shutdown()
            return
        }

        data.clearData()

        // upload everyone (health check was done before)
        // if emergency dump is older than database dump, it will NOT be uploaded
        // MUST NOT be async
        emergency_ss.uploadAllDumpsToDatabase(false)

        // 3.
        for (player in plugin.server.onlinePlayers) {
            data.getOnlinePlayers()[player.uniqueId] = player.name
            registration_ss.register(player.uniqueId, player)
        }

        isRestarting = false

        // 4.
        plugin.propEventsInitless.fireEvent(PlayerDataRestartEvent())
    }

    fun stop() {
        logger.log("Stopping...", LogLevel.LOW)
        isRestarting = true

        // unregister everyone
        for (playerdata in data.getPlayerDataMap().values) {
            // this step MUST NOT be async.
            // IF DATABASE HEALTH FAILS,
            // EMERGENCY DUMP IS AUTOMATICALLY CREATED IN OUTPUT_SS

            val player = runCatching {
                plugin.server.getPlayer(playerdata.uuid)
            }.getOrElse {
                logger.log("Player $playerdata.uuid not found", LogLevel.HIGH)
                return
            }

            registration_ss.unregister(playerdata.uuid, player, false)
        }
    }

    fun fromJson(json: String?): PlayerData? = Gson().fromJson(json, PlayerData::class.java)
}
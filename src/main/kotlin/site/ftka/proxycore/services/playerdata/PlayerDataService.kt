package site.ftka.proxycore.services.playerdata

import com.google.gson.Gson
import site.ftka.proxycore.MClass
import site.ftka.proxycore.services.logging.LoggingService.LogLevel
import site.ftka.proxycore.services.logging.objects.ServiceLogger
import site.ftka.proxycore.services.playerdata.events.PlayerDataRegisterEvent
import site.ftka.proxycore.services.playerdata.events.PlayerDataUnregisterEvent
import site.ftka.proxycore.services.playerdata.listeners.PlayerDataListener
import site.ftka.proxycore.services.playerdata.objects.PlayerData
import site.ftka.proxycore.services.playerdata.subservices.PlayerDataInputSubservice
import site.ftka.proxycore.services.playerdata.subservices.PlayerDataOutputSubservice
import java.util.UUID

class PlayerDataService(val plugin: MClass) {
    val services = plugin.services()

    val logger: ServiceLogger = services.loggingService.getLog("PlayerData", "&3[PDATA]")

    // subservices
    val pdiss = PlayerDataInputSubservice(plugin)
    val pdoss = PlayerDataOutputSubservice(plugin)

    init {
        logger.log("&eInitializing PlayerData service.", LogLevel.LOW)

        // initialize listeners
        plugin.onProxyInitRunnables.add{
            plugin.server.eventManager.register(plugin, PlayerDataListener(plugin))
        }
    }

    // playerDataMap saves player's playerdata
    // playerUsernameMap saves player's UUID->Username data
    // onlinePlayers is controlled by PlayerDataListener
    private val playerDataMap: MutableMap<UUID, PlayerData> = mutableMapOf()
    private val playerUsernameMap: MutableMap<UUID, String> = mutableMapOf()
    val onlinePlayers: MutableMap<UUID, String> = mutableMapOf()

    fun getCachedData(): MutableMap<UUID, PlayerData> = playerDataMap

    fun getCachedPlayer(uuid: UUID): PlayerData? = playerDataMap[uuid]

    fun isCached(uuid: UUID): Boolean = playerDataMap.containsKey(uuid)

    // Register functions
    // 1. Obtain or create player's information
    // 2. Store in cache
    // 3. Report event
    fun register(uuid: UUID) {
        var playerdata: PlayerData? = null

        logger.log("Starting registration for uuid ($uuid)", LogLevel.DEBUG)

        // 1.
        services.dbService.exists(uuid.toString()).whenCompleteAsync { existsResult, _ ->
            // If existsResult, then get
            // If not, then create
            if (!existsResult) {
                logger.log("Creating new playerdata ($uuid)")
                playerdata = pdoss.create(uuid)
                return@whenCompleteAsync
            }

            logger.log("Retrieving playerdata for uuid ($uuid)", LogLevel.HIGH)
            pdiss.get(uuid).whenComplete { getResult, _ -> playerdata = getResult }
        }

        logger.log("Caching playerdata for uuid ($uuid)", LogLevel.DEBUG)
        // 2.
        if (playerdata == null) return // this should never happen
        playerDataMap[uuid] = playerdata!!
        playerUsernameMap[uuid] = playerdata!!.username

        logger.log("Successfully cached playerdata for ${playerdata!!.username} ($uuid)", LogLevel.DEBUG)

        // 3.
        plugin.server.eventManager.fire(PlayerDataRegisterEvent(uuid, playerdata))
    }

    // 1. Save player's information in database
    // 2. Remove from cache
    // 3. Report event
    fun unregister(uuid: UUID) {
        val playerdata = playerDataMap[uuid] ?: return

        // 1.
        pdoss.set(uuid, playerdata)

        // 2.
        playerDataMap.remove(uuid)
        playerUsernameMap.remove(uuid)

        logger.log("Unregistered playerdata: ($uuid)", LogLevel.DEBUG)

        // 3.
        plugin.server.eventManager.fire(PlayerDataUnregisterEvent(uuid, playerdata))
    }

    fun fromJson(json: String?): PlayerData? = Gson().fromJson(json, PlayerData::class.java)
}
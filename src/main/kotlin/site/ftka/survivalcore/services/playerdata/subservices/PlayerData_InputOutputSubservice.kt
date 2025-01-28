package site.ftka.survivalcore.services.playerdata.subservices

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import java.util.UUID
import java.util.concurrent.CompletableFuture

internal class PlayerData_InputOutputSubservice(private val service: PlayerDataService, private val plugin: MClass) {

    private val logger = service.logger.sub("db_inOut")
    private val essFwk = plugin.essentialsFwk

    /*
        GGGGGG  EEEEEE  TTTTTTTT
        GG      EE         TT
        GG GGG  EEEE       TT
        GG  GG  EE         TT
        GGGGGG  EEEEEE     TT
    */

    // Will save last 10 requests just in case a backup is needed
    private val requestsBuffer = mutableMapOf<UUID, String?>()
    fun getRequestBuffer(): Map<UUID, String?> {
        return requestsBuffer
    }

    private fun saveRequest(uuid: UUID, result: String?) {
        if (requestsBuffer.size >= 10)
            requestsBuffer.remove(requestsBuffer.keys.first())
        requestsBuffer[uuid] = result
    }

    fun get(uuid: UUID, async: Boolean = true): CompletableFuture<PlayerData?>? {
        // sync
        logger.log("Getting playerdata (async = $async) for $uuid", LogLevel.DEBUG)


        // Case 1: PlayerData is about to be uploaded to the database
        // The most recent version of playerdata is here.
        if (queuedPlayerData.containsKey(uuid)) {
            logger.log("PlayerData found in queuedPlayerData", LogLevel.DEBUG)
            return CompletableFuture.completedFuture(queuedPlayerData[uuid])
        }

        // Case 2: Playerdata is found inside the caching service
        if (service.caching_ss.isCached(uuid)) {
            logger.log("PlayerData found in caching service", LogLevel.DEBUG)
            return CompletableFuture.completedFuture(service.caching_ss.getCachedPlayerData(uuid))
        }

        // Case 3: Playerdata has already been loaded
        if (service.data.exists(uuid))
            return CompletableFuture.completedFuture(service.data.getPlayerData(uuid))

        // Case 4: Playerdata is found in the database
        logger.log("Getting playerdata from database", LogLevel.DEBUG)
        val futureString = essFwk.database.get(uuid.toString(), async)
        return futureString?.thenApply{
            saveRequest(uuid, it)
            logger.log("Got playerdata from database.", LogLevel.DEBUG)
            service.fromJson(it)
        }
    }

    fun exists(uuid: UUID, async: Boolean = true): CompletableFuture<Boolean>? {
        return essFwk.database.exists(uuid.toString(), async)
    }

    /*
        SSSSSS  EEEEEE  TTTTTTTT
        SS      EE         TT
        SSSSSS  EEEE       TT
            SS  EE         TT
        SSSSSS  EEEEEE     TT
    */

    /*
    There could be a case where the most recent playerdata is neither in the Database nor the cache.

    Let's suppose the database has a 200 millisecond delay
    If playerdata is asynchronously sent in millisecond 0,
    any request of this playerdata to database will be outdated from millisecond 1-199.

    To prevent this, queuedPlayerData stores the most recent, pending to be saved playerdata
    and will be accessed if "get" methods are executed for this playerdata in milliseconds 1-199
    */
    private val queuedPlayerData = mutableMapOf<UUID, PlayerData>()

    // Sólo guarda/reescribe la información de la base de datos.
    fun set(playerdata: PlayerData, async: Boolean = true): CompletableFuture<Boolean> {
        // sync (no queuedPlayerData stuff needed here)
        if (!async) {
            val operation =
                essFwk.database.set(playerdata.uuid.toString(), playerdata.toJson(), async).get()
            if (!operation) service.emergency_ss.emergencyDump(playerdata)

            // cache it
            service.caching_ss.storeLatestPlayerData(playerdata)

            return CompletableFuture.completedFuture(operation)
        }

        // async
        queuedPlayerData[playerdata.uuid] = playerdata

        // Set
        val future = essFwk.database.set(playerdata.uuid.toString(), playerdata.toJson())

        // Remove from queuedPlayerData when done
        // If database set failed, emergency dump playerdata
        future.whenComplete { result, _ ->
            queuedPlayerData.remove(playerdata.uuid)

            // Emergency dump
            if (!result) service.emergency_ss.emergencyDump(playerdata)
        }

        // cache it
        service.caching_ss.storeLatestPlayerData(playerdata)

        return future
    }

    /*
        BBBBBB  OOOOOO  TTTTTTTT HH  HH
        BB  BB  OO  OO     TT    HH  HH
        BBBBB   OO  OO     TT    HHHHHH
        BB  BB  OO  OO     TT    HH  HH
        BBBBBB  OOOOOO     TT    HH  HH
     */

    // concurrent playerdata modifications prevention
    private val activeModificationsMutex = mutableMapOf<UUID, Mutex>()
    private val modificationsMapMutex = Mutex()

    // 1. take playerdata (locally or from database)
    // 2. modify it
    // 3. put it back
    suspend fun makeModification(uuid: UUID, modification: (PlayerData) -> Boolean): PlayerDataModificationResult {
        // get player's mutex
        val playerMutex = modificationsMapMutex.withLock {
            activeModificationsMutex.getOrPut(uuid) { Mutex() }
        }

        logger.log("Triggered PlayerData modification for $uuid", LogLevel.DEBUG)

        playerMutex.withLock {
            // take playerdata
            val pdata = service.data.getPlayerData(uuid) ?: kotlin.run {
                exists(uuid) ?: return PlayerDataModificationResult.FAILURE_PLAYERDATA_UNAVAILABLE
                get(uuid) ?: return PlayerDataModificationResult.FAILURE_CORRUPT_PLAYERDATA
            }

            // modify it
            if (pdata is PlayerData) {
                if (modification(pdata)) {
                    // put it back
                    if (service.data.getPlayerDataMap().containsKey(uuid))
                        service.data.putPlayerData(uuid, pdata)
                    set(pdata)
                    return PlayerDataModificationResult.SUCCESS
                } else
                    return PlayerDataModificationResult.FAILURE_UNKNOWN
            } else {
                (pdata as CompletableFuture<*>).thenApply {
                    if (modification(it as PlayerData)) {
                        // put it back
                        if (service.data.getPlayerDataMap().containsKey(uuid))
                            service.data.putPlayerData(uuid, it)
                        set(it)
                        return@thenApply PlayerDataModificationResult.SUCCESS
                    } else
                        return@thenApply PlayerDataModificationResult.FAILURE_UNKNOWN
                }
            }

            return PlayerDataModificationResult.FAILURE_UNKNOWN
        }
    }

    enum class PlayerDataModificationResult {
        SUCCESS,
        FAILURE_PLAYERDATA_UNAVAILABLE,
        FAILURE_CORRUPT_PLAYERDATA,
        FAILURE_UNKNOWN
    }
}
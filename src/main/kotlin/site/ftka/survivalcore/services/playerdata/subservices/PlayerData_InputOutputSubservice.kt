package site.ftka.survivalcore.services.playerdata.subservices

import kotlinx.coroutines.future.await
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

    fun get(uuid: UUID, async: Boolean = true): CompletableFuture<PlayerData?> {
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
        return futureString.thenApply {
            saveRequest(uuid, it)
            logger.log("Got playerdata from database.", LogLevel.DEBUG)
            service.fromJson(it)
        }
    }

    fun exists(uuid: UUID, async: Boolean = true): CompletableFuture<Boolean> {
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

    // 1. take playerdata (locally or from database)
    // 2. modify it
    // 3. put it back
    suspend fun makeModification(uuid: UUID, modification: (PlayerData) -> Boolean): PlayerDataModificationResult {
        val lock = service.data.getLock(uuid)

        logger.log("Triggered PlayerData modification for $uuid", LogLevel.DEBUG)

        return lock.withLock {
            val isOnline = service.data.exists(uuid)

            // 1. Obtain the PlayerData object (Memory if online, else DB/Cache)
            val pdata: PlayerData = if (isOnline) {
                service.data.getPlayerData(uuid)
            } else {
                get(uuid, async = true).await()
            } ?: return@withLock PlayerDataModificationResult.FAILURE_PLAYERDATA_UNAVAILABLE

            // 2. Apply modification
            val success = try {
                modification(pdata)
            } catch (e: Exception) {
                logger.log("Exception during PlayerData modification for $uuid: ${e.message}", LogLevel.LOW)
                e.printStackTrace()
                false
            }

            if (!success) return@withLock PlayerDataModificationResult.FAILURE_UNKNOWN

            // 3. Save strategy
            val result = if (isOnline) {
                // For online players, we update memory.
                // Database persistence happens when they quit or via autosave.
                service.data.putPlayerData(uuid, pdata)
                PlayerDataModificationResult.SUCCESS
            } else {
                // For offline players, we MUST save immediately or the change is lost.
                val saved = set(pdata, async = true).await()
                if (saved) PlayerDataModificationResult.SUCCESS
                else PlayerDataModificationResult.FAILURE_UNKNOWN
            }

            // 4. Final cleanup
            if (!isOnline) service.data.cleanupLock(uuid)

            return@withLock result
        }
    }


    enum class PlayerDataModificationResult {
        SUCCESS,
        FAILURE_PLAYERDATA_UNAVAILABLE,
        FAILURE_CORRUPT_PLAYERDATA,
        FAILURE_UNKNOWN
    }
}
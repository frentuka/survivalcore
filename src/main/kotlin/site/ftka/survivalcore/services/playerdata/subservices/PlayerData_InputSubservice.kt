package site.ftka.survivalcore.services.playerdata.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import java.util.UUID
import java.util.concurrent.CompletableFuture

class PlayerData_InputSubservice(private val service: PlayerDataService, private val plugin: MClass) {

    private val logger = service.logger.sub("db_in")
    private val essFwk = plugin.essentialsFwk

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
        if (service.output_ss.queuedPlayerData.containsKey(uuid)) {
            logger.log("PlayerData found in queuedPlayerData", LogLevel.DEBUG)
            return CompletableFuture.completedFuture(service.output_ss.queuedPlayerData[uuid])
        }

        // Case 2: Playerdata is found inside the caching service
        if (service.caching_ss.isCached(uuid)) {
            logger.log("PlayerData found in caching service", LogLevel.DEBUG)
            return CompletableFuture.completedFuture(service.caching_ss.getCachedPlayerData(uuid))
        }

        // Case 3: Playerdata is found in the database
        logger.log("Getting playerdata from database", LogLevel.DEBUG)
        val futureString = essFwk.database.get(uuid.toString(), async)
        return futureString?.thenApply{
            saveRequest(uuid, it)
            logger.log("Got playerdata from database. Request buffer: ${getRequestBuffer().values}", LogLevel.DEBUG)
            service.fromJson(it)
        }
    }

    fun exists(uuid: UUID, async: Boolean = true): CompletableFuture<Boolean>? {
        return essFwk.database.exists(uuid.toString(), async)
    }
}
package site.ftka.survivalcore.services.playerdata.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.UUID
import java.util.concurrent.CompletableFuture

class PlayerData_InputSubservice(private val service: PlayerDataService, private val plugin: MClass) {

    // fast access vals
    private val essFwk = plugin.essentialsFwk

    fun get(uuid: UUID, async: Boolean = true): CompletableFuture<PlayerData?>? {
        // sync
        if (!async) {
            // If queuedPlayerData contains this uuid key,
            // the most recent version of the playerdata is in there.
            if (service.output_ss.queuedPlayerData.containsKey(uuid))
                return CompletableFuture.completedFuture(service.output_ss.queuedPlayerData[uuid])

            // Also, playerdata could be found inside the caching service
            if (service.caching_ss.isCached(uuid))
                return CompletableFuture.completedFuture(service.caching_ss.getCachedPlayerData(uuid))

            return essFwk.database.get(uuid.toString(), false)?.thenApply { service.fromJson(it) }
        }

        // async
        // If queuedPlayerData contains this uuid key,
        // the most recent version of the playerdata is in there.
        if (service.output_ss.queuedPlayerData.containsKey(uuid))
            return CompletableFuture.completedFuture(service.output_ss.queuedPlayerData[uuid])

        // Also, playerdata could be found inside the caching service
        if (service.caching_ss.isCached(uuid))
            return CompletableFuture.completedFuture(service.caching_ss.getCachedPlayerData(uuid))

        val futureString = essFwk.database.get(uuid.toString())
        return futureString?.thenApply{ service.fromJson(it) }
    }

    fun exists(uuid: UUID, async: Boolean = true): CompletableFuture<Boolean>? {
        return essFwk.database.exists(uuid.toString(), async)
    }
}
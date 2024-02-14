package site.ftka.survivalcore.services.playerdata.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*
import java.util.concurrent.CompletableFuture

class PlayerData_OutputSubservice(private val service: PlayerDataService, private val plugin: MClass) {

    // fast access vals
    private val essFwk = plugin.essentialsFwk

    /*
        There could be a case where the most recent playerdata is neither in the Database nor the cache.

        Let's suppose the database has a 200 millisecond delay
        If playerdata is asynchronously sent in millisecond 0,
        any request of this playerdata to database will be outdated from millisecond 1-199.

        To prevent this, queuedPlayerData stores the most recent, pending to be saved playerdata
        and will be accessed if "get" methods are executed for this playerdata in milliseconds 1-199
    */
    val queuedPlayerData = mutableMapOf<UUID, PlayerData>()

    // Sólo guarda/reescribe la información de la base de datos.
    fun set(playerdata: PlayerData, async: Boolean = true): CompletableFuture<Boolean> {
        // sync (no queuedPlayerData stuff needed here)
        if (!async) {
            val operation =
                essFwk.database.set(playerdata.uuid.toString(), playerdata.toJson()).get()
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
}
package site.ftka.survivalcore.services.playerdata.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*
import java.util.concurrent.CompletableFuture

class PlayerDataOutputSubservice(private val service: PlayerDataService, private val plugin: MClass) {
    // PlayerData setter
    // Escribir informacion directamente a la base de datos.

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
    fun asyncSet(playerdata: PlayerData): CompletableFuture<Boolean> {
        queuedPlayerData[playerdata.uuid] = playerdata

        // Set
        val future = plugin.dbEssential.asyncSet(playerdata.uuid.toString(), playerdata.toJson())

        // Remove from queuedPlayerData when done
        // If database set failed, emergency dump playerdata
        future.whenComplete { result, _ ->
            queuedPlayerData.remove(playerdata.uuid)

            // Emergency dump
            if (!result) service.emergency_ss.emergencyDump(playerdata)
        }

        return future
    }

    // No need to implement queuedPlayerData here as this will stop the whole program until set is done.
    fun syncSet(playerdata: PlayerData): Boolean {
        // Realizar cambios
        return plugin.dbEssential.syncSet(playerdata.uuid.toString(), playerdata.toJson())
    }

    // Crear playerdata, luego guardar
    fun create(uuid: UUID): PlayerData? {
        var playerdata: PlayerData? = null
        plugin.server.getPlayer(uuid)?.let {
            if (!it.isOnline) return null
            playerdata = PlayerData(uuid, it.name)
            asyncSet(PlayerData(uuid, it.name))
        }

        return playerdata
    }

}
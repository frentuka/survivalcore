package site.ftka.survivalcore.services.playerdata.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.UUID
import java.util.concurrent.CompletableFuture

class PlayerData_InputSubservice(private val service: PlayerDataService, private val plugin: MClass) {
    // PlayerData getter
    // Recibir informacion directamente de la base de datos.

    val uuid_playerdata_sufix = "_data"

    fun asyncGet(uuid: UUID): CompletableFuture<PlayerData?> {
        // If queuedPlayerData contains this uuid key,
        // the most recent version of the playerdata is in there.
        if (service.output_ss.queuedPlayerData.containsKey(uuid))
            return CompletableFuture.completedFuture(service.output_ss.queuedPlayerData[uuid])

        val futureString = plugin.dbEssential.asyncGet(uuid.toString() + uuid_playerdata_sufix)
        return futureString?.thenApply{ service.fromJson(it) }
            ?: CompletableFuture.completedFuture(null)
    }

    fun syncGet(uuid: UUID): PlayerData? {
        // If queuedPlayerData contains this uuid key,
        // the most recent version of the playerdata is in there.
        if (service.output_ss.queuedPlayerData.containsKey(uuid))
            return service.output_ss.queuedPlayerData[uuid]

        return service.fromJson(plugin.dbEssential.syncGet(uuid.toString() + uuid_playerdata_sufix))
    }

    fun asyncExists(uuid: UUID): CompletableFuture<Boolean>? {
        return plugin.dbEssential.asyncExists(uuid.toString())
    }

    fun syncExists(uuid: UUID): Boolean? {
        return plugin.dbEssential.syncExists(uuid.toString())
    }
}
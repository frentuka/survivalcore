package site.ftka.survivalcore.services.playerdata.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.UUID
import java.util.concurrent.CompletableFuture

class PlayerDataInputSubservice(private val service: PlayerDataService, private val plugin: MClass) {
    val services = plugin.services
    // PlayerData getter
    // Recibir informacion directamente de la base de datos.

    fun asyncGet(uuid: UUID): CompletableFuture<PlayerData?> {
        // If queuedPlayerData contains this uuid key,
        // the most recent version of the playerdata is in there.
        if (service.output_ss.queuedPlayerData.containsKey(uuid))
            return CompletableFuture.completedFuture(service.output_ss.queuedPlayerData[uuid])

        val futureString = services.dbService.asyncGet(uuid.toString())
        return futureString.thenApply{ services.playerDataService.fromJson(it) }
    }

    fun syncGet(uuid: UUID): PlayerData? {
        // If queuedPlayerData contains this uuid key,
        // the most recent version of the playerdata is in there.
        if (service.output_ss.queuedPlayerData.containsKey(uuid))
            return service.output_ss.queuedPlayerData[uuid]

        return service.fromJson(services.dbService.syncGet(uuid.toString()))
    }
}
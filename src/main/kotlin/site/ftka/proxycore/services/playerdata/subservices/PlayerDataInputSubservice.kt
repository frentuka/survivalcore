package site.ftka.proxycore.services.playerdata.subservices

import site.ftka.proxycore.MClass
import site.ftka.proxycore.services.playerdata.objects.PlayerData
import java.util.UUID
import java.util.concurrent.CompletableFuture

class PlayerDataInputSubservice(val plugin: MClass) {
    val services = plugin.services()
    // PlayerData getter
    // Recibir informacion directamente de la base de datos.

    fun get(uuid: UUID): CompletableFuture<PlayerData?> {
        val futureString = services.dbService.get(uuid.toString())
        return futureString.thenApply{ services.playerDataService.fromJson(it) }
    }
}
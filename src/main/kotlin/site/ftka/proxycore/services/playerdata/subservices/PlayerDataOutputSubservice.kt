package site.ftka.proxycore.services.playerdata.subservices

import site.ftka.proxycore.MClass
import site.ftka.proxycore.services.playerdata.objects.PlayerData
import site.ftka.proxycore.services.playerdata.thowables.PlayerDataUUIDMismatchException
import java.util.*

class PlayerDataOutputSubservice(val plugin: MClass) {
    val services = plugin.services()
    // PlayerData setter
    // Escribir informacion directamente a la base de datos.

    // Sólo guarda/reescribe la información de la base de datos.
    fun set(uuid: UUID, playerdata: PlayerData) {
        // Las UUID son congruentes?
        if (playerdata.uuid != uuid) throw PlayerDataUUIDMismatchException(uuid, playerdata)

        // Realizar cambios
        services.dbService.set(uuid.toString(), playerdata.toJson())

        println("[PlayerData] setted playerdata: ${playerdata.username} (${playerdata.uuid})")
    }

    // Crear playerdata, luego guardar
    fun create(uuid: UUID): PlayerData? {
        var playerdata: PlayerData? = null
        plugin.server.getPlayer(uuid).ifPresent {
            playerdata = PlayerData(uuid, it.username)
            set(uuid, PlayerData(uuid, it.username))
        }

        return playerdata
    }

}
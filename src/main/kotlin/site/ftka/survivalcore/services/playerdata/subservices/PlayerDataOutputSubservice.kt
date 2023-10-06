package site.ftka.survivalcore.services.playerdata.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import site.ftka.survivalcore.services.playerdata.thowables.PlayerDataUUIDMismatchException
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
        plugin.server.getPlayer(uuid)?.let {
            if (!it.isOnline) return null
            playerdata = PlayerData(uuid, it.name)
            set(uuid, PlayerData(uuid, it.name))
        }

        return playerdata
    }

}
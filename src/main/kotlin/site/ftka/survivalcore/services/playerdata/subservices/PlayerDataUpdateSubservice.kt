package site.ftka.survivalcore.services.playerdata.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.objects.PlayerData

class PlayerDataUpdateSubservice(private val service: PlayerDataService, private val plugin: MClass) {

    /*
        Receive ModifiablePlayerData and update
        into PlayerDataService's local cache
        or save it into database
     */

    // 1. If local cache contains playerdata, overwrite there
    // 2. If not, overwrite in database.
    fun update(playerdata: PlayerData) {
        // 1.
        if (service.playerDataMap.containsKey(playerdata.uuid))
            service.playerDataMap[playerdata.uuid] = playerdata

        // 2.
        else service.output_ss.asyncSet(playerdata.uuid, playerdata)
    }

}
package site.ftka.survivalcore.services.playerdata
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.UUID

class PlayerDataAPI(private val svc: PlayerDataService) {

    fun exists(uuid: UUID)
        = svc.data.getPlayerDataMap().containsKey(uuid)

    fun getPlayerData_locally(uuid: UUID)
        = svc.data.getPlayerData(uuid)

    fun getPlayerData(uuid: UUID)
        = svc.inout_ss.get(uuid)

}
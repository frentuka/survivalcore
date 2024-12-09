package site.ftka.survivalcore.services.playerdata
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.UUID
import java.util.concurrent.CompletableFuture

class PlayerDataAPI(private val svc: PlayerDataService) {

    fun exists(uuid: UUID)
        = svc.getPlayerDataMap().containsKey(uuid)

    fun get(uuid: UUID)
        = svc.getPlayerData(uuid)

}
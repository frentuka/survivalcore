package site.ftka.survivalcore.services.playerdata
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.UUID

class PlayerDataAPI(private val svc: PlayerDataService) {

    fun exists(uuid: UUID)
        = svc.playerDataMap.containsKey(uuid)

    fun get(uuid: UUID)
        = svc.playerDataMap[uuid]

    /** USAGE:
     *  get(), modification AND replace()
     *  must be done ALL INSIDE runBlocking{}
     */
    fun replace(uuid: UUID, newPlayerData: PlayerData) {
        // Check that this is being executed inside main thread.
        if (Thread.currentThread().name != "main") { svc.logger.log("API: PLAYERDATA SHOULD BE EXECUTED IN MAIN THREAD", LogLevel.LOW); return }

        // Check that playerdata's UUID is correct
        if (newPlayerData.uuid != uuid) { svc.logger.log("API: PLAYERDATA UUID MISMATCH ERROR", LogLevel.LOW); return }

        svc.playerDataMap[uuid] = newPlayerData
    }

}
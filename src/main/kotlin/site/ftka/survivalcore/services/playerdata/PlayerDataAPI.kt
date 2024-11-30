package site.ftka.survivalcore.services.playerdata
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.UUID

class PlayerDataAPI(private val svc: PlayerDataService) {

    fun exists(uuid: UUID)
        = svc.getPlayerDataMap().containsKey(uuid)

    fun get(uuid: UUID)
        = svc.getPlayerData(uuid)

    /**
     *  Must be used inside main thread
     */
    fun replace(uuid: UUID, newPlayerData: PlayerData) {
        // Check that this is being executed inside main thread.
        if (Thread.currentThread().name != "main") {
            svc.logger.log("API: PLAYERDATA SHOULD BE EXECUTED IN MAIN THREAD", LogLevel.LOW, NamedTextColor.RED);
            return
        }

        // Check that playerdata's UUID is correct
        if (newPlayerData.uuid != uuid) {
            svc.logger.log("API: PLAYERDATA UUID MISMATCH ERROR", LogLevel.LOW, NamedTextColor.RED);
            return
        }

        svc.putPlayerDataMap(uuid, newPlayerData)
    }

}
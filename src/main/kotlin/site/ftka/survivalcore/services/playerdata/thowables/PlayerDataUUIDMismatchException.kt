package site.ftka.survivalcore.services.playerdata.thowables

import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.UUID

class PlayerDataUUIDMismatchException(val given_uuid: UUID, val given_playerdata: PlayerData):
    Exception("Given UUID does not match with playerdata's UUID. Did not commit any changes.") {
}
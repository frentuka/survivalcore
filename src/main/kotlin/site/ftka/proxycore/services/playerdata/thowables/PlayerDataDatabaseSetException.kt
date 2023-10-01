package site.ftka.proxycore.services.playerdata.thowables

import site.ftka.proxycore.services.playerdata.objects.PlayerData
import java.util.UUID

class PlayerDataDatabaseSetException(uuid: UUID):
    Exception("Something went wrong when trying to set: $uuid") {
}
package site.ftka.survivalcore.services.playerdata.thowables

import java.util.UUID

class PlayerDataDatabaseSetException(uuid: UUID):
    Exception("Something went wrong when trying to set: $uuid") {
}
package site.ftka.survivalcore.services.playerdata.thowables

import java.util.UUID

class PlayerDataDatabaseModificationException(uuid: UUID):
    Exception("Something went wrong when trying to set playerdata for $uuid") {
}
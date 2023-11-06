package site.ftka.survivalcore.services.playerdata.objects.modules

import java.util.UUID

data class PlayerInformation(private val uuid: UUID) {

    var username: String = "{unknown}"
    var lastConnection: Long? = null

}
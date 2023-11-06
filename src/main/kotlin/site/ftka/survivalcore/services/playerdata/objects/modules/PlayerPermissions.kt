package site.ftka.survivalcore.services.playerdata.objects.modules

import java.util.UUID

data class PlayerPermissions(private val uuid: UUID) {

    var normalGroup: Int = 0
    var specialGroup: Int? = null
    var staffGroup: Int? = null

    var permissions: List<String> = listOf()

}
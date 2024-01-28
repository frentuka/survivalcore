package site.ftka.survivalcore.services.playerdata.objects.modules

import java.util.UUID

data class PlayerPermissions(private val uuid: UUID) {

    var groups = listOf<UUID>()

    var permissions: Set<String> = setOf()

}
package site.ftka.survivalcore.services.playerdata.objects.modules

import java.util.UUID

class PlayerPermissions {

    var groups = listOf<UUID>()

    var permissions: Set<String> = setOf()

}
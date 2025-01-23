package site.ftka.survivalcore.services.playerdata.objects.modules

import java.util.UUID

class PlayerPermissions {

    var groups: Set<UUID> = setOf()
    var permissions: Set<String> = setOf()
    var displayGroup: UUID? = null

}
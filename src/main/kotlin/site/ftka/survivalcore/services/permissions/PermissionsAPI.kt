package site.ftka.survivalcore.services.permissions

import java.util.UUID

class PermissionsAPI(private val svc: PermissionsService) {

    fun playerHasPerm(uuid: UUID, permission: String)
        = svc.permissions_ss.playerHasPerm(uuid, permission)

    fun groupHasPerm(uuid: UUID, permission: String)
        = svc.permissions_ss.groupHasPerm(uuid, permission)
}
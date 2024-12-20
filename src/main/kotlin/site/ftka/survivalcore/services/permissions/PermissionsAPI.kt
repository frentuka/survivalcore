package site.ftka.survivalcore.services.permissions

import site.ftka.survivalcore.services.permissions.objects.PermissionGroup
import java.util.UUID

class PermissionsAPI(private val svc: PermissionsService) {

    fun playerHasPerm(uuid: UUID, permission: String)
        = svc.permissions_ss.playerHasPerm(uuid, permission)

    fun groupHasPerm(uuid: UUID, permission: String)
        = svc.permissions_ss.groupHasPerm(uuid, permission)

    /*
        player's permissions
     */

    suspend fun player_addPerm(uuid: UUID, permission: String)
        = svc.players_ss.addPermissionToPlayer(uuid, permission)

    suspend fun player_addPerm(name: String, permission: String)
        = svc.players_ss.addPermissionToPlayer(name, permission)

    suspend fun player_removePerm(uuid: UUID, permission: String)
        = svc.players_ss.removePermissionToPlayer(uuid, permission)

    suspend fun player_removePerm(name: String, permission: String)
        = svc.players_ss.removePermissionToPlayer(name, permission)

    /*
        player's groups
     */

    suspend fun player_addGroup(uuid: UUID, group: String)
        = svc.players_ss.addGroupToPlayer(uuid, group)

    suspend fun player_addGroup(name: String, group: String)
        = svc.players_ss.addGroupToPlayer(name, group)

    suspend fun player_removeGroup(uuid: UUID, group: String)
        = svc.players_ss.removeGroupToPlayer(uuid, group)

    suspend fun player_removeGroup(name: String, group: String)
        = svc.players_ss.removeGroupToPlayer(name, group)

    /*
        group administration
     */

    fun createGroup(name: String)
        = svc.groups_ss.createGroup(name)

    fun deleteGroup(name: String)
        = svc.groups_ss.deleteGroup(name)

    fun renameGroup(name: String, newName: String)
        = svc.groups_ss.renameGroup(name, newName)

    /*
        group's permissions
     */

    fun group_addPerm(uuid: UUID, permission: String)
        = svc.groups_ss.addPermissionToGroup(uuid, permission)

    fun group_addPerm(name: String, permission: String)
        = svc.groups_ss.addPermissionToGroup(name, permission)

    fun group_removePerm(uuid: UUID, permission: String)
        = svc.groups_ss.removePermissionToGroup(uuid, permission)

    fun group_removePerm(name: String, permission: String)
        = svc.groups_ss.removePermissionToGroup(name, permission)

    /*
        group's inheritances
     */

    fun group_addInheritance(uuid: UUID, inheritance: UUID)
        = svc.groups_ss.addInheritanceToGroup(uuid, inheritance)

    fun group_addInheritance(name: String, inheritance: String)
        = svc.groups_ss.addInheritanceToGroup(name, inheritance)

    fun group_removeInheritance(uuid: UUID, inheritance: UUID)
        = svc.groups_ss.removeInheritanceToGroup(uuid, inheritance)

    fun group_removeInheritance(name: String, inheritance: String)
        = svc.groups_ss.removeInheritanceToGroup(name, inheritance)
}
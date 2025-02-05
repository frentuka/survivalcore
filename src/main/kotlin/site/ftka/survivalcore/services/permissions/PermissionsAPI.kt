package site.ftka.survivalcore.services.permissions

import java.util.UUID

class PermissionsAPI(private val svc: PermissionsService) {

    fun player_hasPerm_locally(uuid: UUID, permission: String)
        = svc.permissions_ss.playerHasPerm_locally(uuid, permission)

    fun player_hasPerm(uuid: UUID, permission: String)
        = svc.permissions_ss.playerHasPerm(uuid, permission)

    fun groupHasPerm(uuid: UUID, permission: String)
        = svc.permissions_ss.groupHasPerm(uuid, permission)

    /*
        player's permissions
     */

    fun player_getPerms(player: String, localOnly: Boolean = false)
        = svc.players_ss.getPermissions(player, localOnly)

    fun player_getPerms(uuid: UUID, localOnly: Boolean = false)
        = svc.players_ss.getPermissions(uuid, localOnly)

    //

    suspend fun player_addPerm(uuid: UUID, permission: String)
        = svc.players_ss.addPermission(uuid, permission)

    suspend fun player_addPerm(name: String, permission: String)
        = svc.players_ss.addPermission(name, permission)

    //

    suspend fun player_removePerm(uuid: UUID, permission: String)
        = svc.players_ss.removePermission(uuid, permission)

    suspend fun player_removePerm(name: String, permission: String)
        = svc.players_ss.removePermission(name, permission)

    /*
        player's groups
     */

    fun player_getGroups(player: String, localOnly: Boolean = false)
        = svc.players_ss.getGroups(player, localOnly)

    fun player_getGroups(uuid: UUID, localOnly: Boolean = false)
        = svc.players_ss.getGroups(uuid, localOnly)

    //

    suspend fun player_addGroup(uuid: UUID, group: UUID)
        = svc.players_ss.addGroup(uuid, group)

    suspend fun player_addGroup(uuid: UUID, group: String)
        = svc.players_ss.addGroup(uuid, group)

    suspend fun player_addGroup(name: String, group: String)
        = svc.players_ss.addGroup(name, group)

    //

    suspend fun player_removeGroup(uuid: UUID, group: UUID)
        = svc.players_ss.removeGroup(uuid, group)

    suspend fun player_removeGroup(uuid: UUID, group: String)
        = svc.players_ss.removeGroup(uuid, group)

    suspend fun player_removeGroup(name: String, group: String)
        = svc.players_ss.removeGroup(name, group)

    //

    fun player_getDisplayGroup(player: String, localOnly: Boolean = false)
        = svc.players_ss.getDisplayGroup(player, localOnly)

    fun player_getDisplayGroup(uuid: UUID, localOnly: Boolean = false)
        = svc.players_ss.getDisplayGroup(uuid, localOnly)

    suspend fun player_setDisplayGroup(name: String, group: String)
        = svc.players_ss.setDisplayGroup(name, group)

    suspend fun player_setDisplayGroup(uuid: UUID, group: String)
        = svc.players_ss.setDisplayGroup(uuid, group)

    suspend fun player_setDisplayGroup(uuid: UUID, group: UUID)
        = svc.players_ss.setDisplayGroup(uuid, group)

    /*
        groups
     */

    fun getGroup(group: String)
        = svc.data.getGroup(group)

    fun getGroup(uuid: UUID)
        = svc.data.getGroup(uuid)

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

    //

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

    //

    fun group_removeInheritance(uuid: UUID, inheritance: UUID)
        = svc.groups_ss.removeInheritanceToGroup(uuid, inheritance)

    fun group_removeInheritance(name: String, inheritance: String)
        = svc.groups_ss.removeInheritanceToGroup(name, inheritance)
}
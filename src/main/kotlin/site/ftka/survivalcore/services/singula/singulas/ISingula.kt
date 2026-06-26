package site.ftka.survivalcore.services.singula.singulas

import site.ftka.survivalcore.services.permissions.subservices.PermissionsService_PlayersSubservice.*
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.UUID

interface ISingula {
    val uuid: UUID
    val username: String?

    // Permissions
    suspend fun hasPermission(permission: String): Boolean
    suspend fun getPermissions(): Set<String>
    
    suspend fun addPermission(permission: String): Permissions_addPermissionResult
    suspend fun removePermission(permission: String): Permissions_removePermissionResult

    // Groups
    suspend fun getGroups(): Set<UUID>
    suspend fun getDisplayGroup(): UUID?
    
    suspend fun addGroup(group: UUID): Permissions_addGroupResult
    suspend fun addGroup(group: String): Permissions_addGroupResult
    
    suspend fun removeGroup(group: UUID): Permissions_removeGroupResult
    suspend fun removeGroup(group: String): Permissions_removeGroupResult
    
    suspend fun setDisplayGroup(group: UUID): Permissions_setDisplayGroupResult
    suspend fun setDisplayGroup(group: String): Permissions_setDisplayGroupResult

    // Player Data
    suspend fun getPlayerData(): PlayerData?
}

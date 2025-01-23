package site.ftka.survivalcore.services.permissions.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.permissions.PermissionsService
import site.ftka.survivalcore.services.playerdata.subservices.PlayerData_InputOutputSubservice
import java.util.UUID
import java.util.concurrent.CompletableFuture

class PermissionsService_PlayersSubservice(private val service: PermissionsService, private val plugin: MClass) {

    private fun pdAPI() = plugin.servicesFwk.playerData.api

    /*
        This subservice will be responsible for modifying
        player's groups and permissions.
     */

    /*
        get groups
     */

    fun getGroups(player: String, localOnly: Boolean = false): CompletableFuture<Set<UUID>?> {
        val uuid = plugin.essentialsFwk.usernameTracker.getUUID(player) ?: return CompletableFuture.completedFuture(null)
        return getGroups(uuid)
    }

    fun getGroups(uuid: UUID, localOnly: Boolean = false): CompletableFuture<Set<UUID>?> {
        if (localOnly)
            return CompletableFuture.completedFuture(pdAPI().getPlayerData_locally(uuid)?.permissions?.groups)

        return CompletableFuture<Set<UUID>>().thenApply{
            plugin.servicesFwk.playerData.api.getPlayerData(uuid)?.thenApply {
                it?.permissions?.groups
            }?.get()
        }
    }

    /*
        add group
     */

    enum class Permissions_addGroupResult {
        SUCCESS,
        FAILURE_UNKNOWN,
        FAILURE_CORRUPT_PLAYERDATA,
        FAILURE_PLAYER_UNAVAILABLE,
        FAILURE_GROUP_UNAVAILABLE,
        FAILURE_PLAYER_ALREADY_IN_GROUP
    }

    suspend fun addGroup(player: String, group: String): Permissions_addGroupResult {
        val uuid = plugin.essentialsFwk.usernameTracker.getUUID(player) ?: return Permissions_addGroupResult.FAILURE_PLAYER_UNAVAILABLE
        return addGroup(uuid, group)
    }

    suspend fun addGroup(player: UUID, group: String): Permissions_addGroupResult {
        service.data.getGroup(group) ?: return Permissions_addGroupResult.FAILURE_GROUP_UNAVAILABLE
        return addGroup(player, service.data.getGroup(group)!!.uuid)
    }

    suspend fun addGroup(player: UUID, group: UUID): Permissions_addGroupResult {
        var result: Permissions_addGroupResult
        val modification = plugin.servicesFwk.playerData.inout_ss.makeModification(player) { pdata ->
            // are permissions corrupted?
            pdata.permissions ?:  run {
                Permissions_addGroupResult.FAILURE_CORRUPT_PLAYERDATA
                return@makeModification false
            }
            pdata.permissions?.groups ?: run { pdata.permissions?.groups = setOf() }

            // check group
            if (pdata.permissions?.groups?.contains(group) == true) {
                result = Permissions_addGroupResult.FAILURE_PLAYER_ALREADY_IN_GROUP
                return@makeModification false
            }

            // add to group
            val groups = pdata.permissions?.groups?.toMutableList() ?: mutableListOf()
            groups.add(group)
            pdata.permissions?.groups = groups.toSet()

            result = Permissions_addGroupResult.SUCCESS
            return@makeModification true // true = commit changes to database
        }

        result = when (modification) {
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.SUCCESS -> Permissions_addGroupResult.SUCCESS
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.FAILURE_PLAYERDATA_UNAVAILABLE -> Permissions_addGroupResult.FAILURE_PLAYER_UNAVAILABLE
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.FAILURE_CORRUPT_PLAYERDATA -> Permissions_addGroupResult.FAILURE_CORRUPT_PLAYERDATA
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.FAILURE_UNKNOWN -> Permissions_addGroupResult.FAILURE_UNKNOWN
        }

        return result
    }

    /*
        remove group
     */

    enum class Permissions_removeGroupResult {
        SUCCESS,
        FAILURE_UNKNOWN,
        FAILURE_CORRUPT_PLAYERDATA,
        FAILURE_PLAYER_UNAVAILABLE,
        FAILURE_GROUP_UNAVAILABLE,
        FAILURE_PLAYER_NOT_IN_GROUP
    }

    suspend fun removeGroup(player: String, group: String): Permissions_removeGroupResult {
        val uuid = plugin.essentialsFwk.usernameTracker.getUUID(player) ?: return Permissions_removeGroupResult.FAILURE_PLAYER_UNAVAILABLE
        return removeGroup(uuid, group)
    }

    suspend fun removeGroup(player: UUID, group: String): Permissions_removeGroupResult {
        service.data.getGroup(group) ?: return Permissions_removeGroupResult.FAILURE_GROUP_UNAVAILABLE
        return removeGroup(player, service.data.getGroup(group)!!.uuid)
    }

    suspend fun removeGroup(player: UUID, group: UUID): Permissions_removeGroupResult {
        var result: Permissions_removeGroupResult
        val modification = plugin.servicesFwk.playerData.inout_ss.makeModification(player) { pdata ->
            // are permissions corrupted?
            pdata.permissions ?:  run {
                Permissions_removeGroupResult.FAILURE_CORRUPT_PLAYERDATA
                return@makeModification false
            }
            pdata.permissions?.groups ?: run { pdata.permissions?.groups = setOf() }

            // check group
            if (pdata.permissions?.groups?.contains(group) == false) {
                result = Permissions_removeGroupResult.FAILURE_PLAYER_NOT_IN_GROUP
                return@makeModification false
            }

            // add to group
            val groups = pdata.permissions?.groups?.toMutableList() ?: mutableListOf()
            groups.remove(group)
            pdata.permissions?.groups = groups.toSet()

            result = Permissions_removeGroupResult.SUCCESS
            return@makeModification true // true = commit changes to database
        }

        result = when (modification) {
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.SUCCESS -> Permissions_removeGroupResult.SUCCESS
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.FAILURE_PLAYERDATA_UNAVAILABLE -> Permissions_removeGroupResult.FAILURE_PLAYER_UNAVAILABLE
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.FAILURE_CORRUPT_PLAYERDATA -> Permissions_removeGroupResult.FAILURE_CORRUPT_PLAYERDATA
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.FAILURE_UNKNOWN -> Permissions_removeGroupResult.FAILURE_UNKNOWN
        }

        return result
    }

    /*
        get permissions
     */

    fun getPermissions(player: String, localOnly: Boolean = false): CompletableFuture<Set<String>?> {
        val uuid = plugin.essentialsFwk.usernameTracker.getUUID(player) ?: return CompletableFuture.completedFuture(null)
        return getPermissions(uuid, localOnly)
    }

    fun getPermissions(uuid: UUID, localOnly: Boolean = false): CompletableFuture<Set<String>?> {
        if (localOnly)
            return CompletableFuture.completedFuture(pdAPI().getPlayerData_locally(uuid)?.permissions?.permissions)

        return CompletableFuture<Set<String>>().thenApply{
            plugin.servicesFwk.playerData.api.getPlayerData(uuid)?.thenApply {
                it?.permissions?.permissions
            }?.get()
        }
    }

    /*
        add permission
     */

    enum class Permissions_addPermissionResult {
        SUCCESS,
        FAILURE_UNKNOWN,
        FAILURE_CORRUPT_PLAYERDATA,
        FAILURE_PLAYER_UNAVAILABLE,
        FAILURE_PLAYER_ALREADY_HAS_PERMISSION
    }

    suspend fun addPermission(player: String, permission: String): Permissions_addPermissionResult {
        val uuid = plugin.essentialsFwk.usernameTracker.getUUID(player) ?: return Permissions_addPermissionResult.FAILURE_PLAYER_UNAVAILABLE
        return addPermission(uuid, permission)
    }

    suspend fun addPermission(player: UUID, permission: String): Permissions_addPermissionResult {
        var result: Permissions_addPermissionResult
        val modification = plugin.servicesFwk.playerData.inout_ss.makeModification(player) { pdata ->
            pdata.permissions ?:  run {
                Permissions_addPermissionResult.FAILURE_CORRUPT_PLAYERDATA
                return@makeModification false
            }
            pdata.permissions?.permissions ?: run { pdata.permissions?.permissions = setOf() }

            // check permission
            if (pdata.permissions?.permissions?.contains(permission) == true) {
                result = Permissions_addPermissionResult.FAILURE_PLAYER_ALREADY_HAS_PERMISSION
                return@makeModification false
            }

            // add permission
            val perms = pdata.permissions?.permissions?.toMutableList() ?: mutableListOf()
            perms.add(permission)
            pdata.permissions?.permissions = perms.toSet()

            // apply changes in service
            plugin.servicesFwk.playerData.data.putPlayerData(player, pdata)

            result = Permissions_addPermissionResult.SUCCESS
            return@makeModification true
        }

        result = when(modification) {
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.SUCCESS -> Permissions_addPermissionResult.SUCCESS
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.FAILURE_PLAYERDATA_UNAVAILABLE -> Permissions_addPermissionResult.FAILURE_PLAYER_UNAVAILABLE
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.FAILURE_CORRUPT_PLAYERDATA -> Permissions_addPermissionResult.FAILURE_CORRUPT_PLAYERDATA
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.FAILURE_UNKNOWN -> Permissions_addPermissionResult.FAILURE_UNKNOWN
        }

        return result
    }

    /*
        remove permission
     */

    enum class Permissions_removePermissionResult {
        SUCCESS,
        FAILURE_UNKNOWN,
        FAILURE_CORRUPT_PLAYERDATA,
        FAILURE_PLAYER_UNAVAILABLE,
        FAILURE_PLAYER_DOESNT_HAVE_PERMISSION
    }

    suspend fun removePermission(player: String, permission: String): Permissions_removePermissionResult {
        val uuid = plugin.essentialsFwk.usernameTracker.getUUID(player) ?: return Permissions_removePermissionResult.FAILURE_PLAYER_UNAVAILABLE
        return removePermission(uuid, permission)
    }

    suspend fun removePermission(player: UUID, permission: String): Permissions_removePermissionResult {
        var result: Permissions_removePermissionResult
        val modification = plugin.servicesFwk.playerData.inout_ss.makeModification(player) { pdata ->
            pdata.permissions ?:  run {
                Permissions_addPermissionResult.FAILURE_CORRUPT_PLAYERDATA
                return@makeModification false
            }
            pdata.permissions?.permissions ?: run { pdata.permissions?.permissions = setOf() }

            // check permission
            if (pdata.permissions?.permissions?.contains(permission) == false) {
                result = Permissions_removePermissionResult.FAILURE_PLAYER_DOESNT_HAVE_PERMISSION
                return@makeModification false
            }

            // remove permission
            val perms = pdata.permissions?.permissions?.toMutableList() ?: mutableListOf()
            perms.remove(permission)
            pdata.permissions?.permissions = perms.toSet()

            // apply changes in service
            plugin.servicesFwk.playerData.data.putPlayerData(player, pdata)

            result = Permissions_removePermissionResult.SUCCESS
            return@makeModification true
        }

        result = when(modification) {
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.SUCCESS -> Permissions_removePermissionResult.SUCCESS
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.FAILURE_PLAYERDATA_UNAVAILABLE -> Permissions_removePermissionResult.FAILURE_PLAYER_UNAVAILABLE
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.FAILURE_CORRUPT_PLAYERDATA -> Permissions_removePermissionResult.FAILURE_CORRUPT_PLAYERDATA
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.FAILURE_UNKNOWN -> Permissions_removePermissionResult.FAILURE_UNKNOWN
        }

        return result
    }

    /*
        get display group
     */

    fun getDisplayGroup(player: String, localOnly: Boolean = false): CompletableFuture<UUID?> {
        val uuid = plugin.essentialsFwk.usernameTracker.getUUID(player) ?: return CompletableFuture.completedFuture(null)
        return getDisplayGroup(uuid, localOnly)
    }

    fun getDisplayGroup(uuid: UUID, localOnly: Boolean = false): CompletableFuture<UUID?> {
        if (localOnly)
            return CompletableFuture.completedFuture(pdAPI().getPlayerData_locally(uuid)?.permissions?.displayGroup)

        return plugin.servicesFwk.playerData.api.getPlayerData(uuid)?.thenApply {
            it?.permissions?.displayGroup
        } ?: CompletableFuture.completedFuture(null)
    }

    /*
        set display group
     */

    enum class Permissions_setDisplayGroupResult {
        SUCCESS,
        FAILURE_UNKNOWN,
        FAILURE_CORRUPT_PLAYERDATA,
        FAILURE_PLAYER_UNAVAILABLE,
        FAILURE_PLAYER_NOT_IN_GROUP
    }

    suspend fun setDisplayGroup(player: String, group: String): Permissions_setDisplayGroupResult {
        val uuid = plugin.essentialsFwk.usernameTracker.getUUID(player) ?: return Permissions_setDisplayGroupResult.FAILURE_PLAYER_UNAVAILABLE
        return setDisplayGroup(uuid, service.data.getGroup(group)?.uuid ?: return Permissions_setDisplayGroupResult.FAILURE_UNKNOWN)
    }

    suspend fun setDisplayGroup(uuid: UUID, group: String): Permissions_setDisplayGroupResult {
        return setDisplayGroup(uuid, service.data.getGroup(group)?.uuid ?: return Permissions_setDisplayGroupResult.FAILURE_UNKNOWN)
    }

    suspend fun setDisplayGroup(uuid: UUID, group: UUID): Permissions_setDisplayGroupResult {
        var result: Permissions_setDisplayGroupResult

        val modification = plugin.servicesFwk.playerData.inout_ss.makeModification(uuid) { pdata ->
            // corrupt pdata
            pdata.permissions ?:  run {
                result = Permissions_setDisplayGroupResult.FAILURE_CORRUPT_PLAYERDATA
                return@makeModification false
            }

            // player not in group to display
            if (pdata.permissions?.groups?.contains(group) ?: false) {
                result = Permissions_setDisplayGroupResult.FAILURE_PLAYER_NOT_IN_GROUP
                return@makeModification false
            }

            // do not commit unnecessary changes
            if (pdata.permissions?.displayGroup == group) {
                return@makeModification false
            }

            // commit changes
            pdata.permissions?.displayGroup = group
            return@makeModification true
        }

        result = when(modification) {
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.SUCCESS -> Permissions_setDisplayGroupResult.SUCCESS
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.FAILURE_PLAYERDATA_UNAVAILABLE -> Permissions_setDisplayGroupResult.FAILURE_PLAYER_UNAVAILABLE
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.FAILURE_CORRUPT_PLAYERDATA -> Permissions_setDisplayGroupResult.FAILURE_CORRUPT_PLAYERDATA
            PlayerData_InputOutputSubservice.PlayerDataModificationResult.FAILURE_UNKNOWN -> Permissions_setDisplayGroupResult.FAILURE_UNKNOWN
        }

        return result
    }

}
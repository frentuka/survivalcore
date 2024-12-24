package site.ftka.survivalcore.services.permissions.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.permissions.PermissionsService
import site.ftka.survivalcore.services.playerdata.subservices.PlayerData_InputOutputSubservice
import java.util.UUID

class PermissionsService_PlayersSubservice(private val service: PermissionsService, private val plugin: MClass) {

    /*
        This subservice will be responsible for modifying
        player's groups and permissions.
     */

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

    suspend fun addGroupToPlayer(player: String, group: String): Permissions_addGroupResult {
        val uuid = plugin.essentialsFwk.usernameTracker.getUUID(player) ?: return Permissions_addGroupResult.FAILURE_PLAYER_UNAVAILABLE
        return addGroupToPlayer(uuid, group)
    }

    suspend fun addGroupToPlayer(player: UUID, group: String): Permissions_addGroupResult {
        service.maps_ss.getGroup(group) ?: return Permissions_addGroupResult.FAILURE_GROUP_UNAVAILABLE
        return addGroupToPlayer(player, service.maps_ss.getGroup(group)!!.uuid)
    }

    suspend fun addGroupToPlayer(player: UUID, group: UUID): Permissions_addGroupResult {
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

    suspend fun removeGroupToPlayer(player: String, group: String): Permissions_removeGroupResult {
        val uuid = plugin.essentialsFwk.usernameTracker.getUUID(player) ?: return Permissions_removeGroupResult.FAILURE_PLAYER_UNAVAILABLE
        return removeGroupToPlayer(uuid, group)
    }

    suspend fun removeGroupToPlayer(player: UUID, group: String): Permissions_removeGroupResult {
        service.maps_ss.getGroup(group) ?: return Permissions_removeGroupResult.FAILURE_GROUP_UNAVAILABLE
        return removeGroupToPlayer(player, service.maps_ss.getGroup(group)!!.uuid)
    }

    suspend fun removeGroupToPlayer(player: UUID, group: UUID): Permissions_removeGroupResult {
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
        add permission
     */

    enum class Permissions_addPermissionResult {
        SUCCESS,
        FAILURE_UNKNOWN,
        FAILURE_CORRUPT_PLAYERDATA,
        FAILURE_PLAYER_UNAVAILABLE,
        FAILURE_PLAYER_ALREADY_HAS_PERMISSION
    }

    suspend fun addPermissionToPlayer(player: String, permission: String): Permissions_addPermissionResult {
        val uuid = plugin.essentialsFwk.usernameTracker.getUUID(player) ?: return Permissions_addPermissionResult.FAILURE_PLAYER_UNAVAILABLE
        return addPermissionToPlayer(uuid, permission)
    }

    suspend fun addPermissionToPlayer(player: UUID, permission: String): Permissions_addPermissionResult {
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
            plugin.servicesFwk.playerData.putPlayerDataMap(player, pdata)

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

    suspend fun removePermissionToPlayer(player: String, permission: String): Permissions_removePermissionResult {
        val uuid = plugin.essentialsFwk.usernameTracker.getUUID(player) ?: return Permissions_removePermissionResult.FAILURE_PLAYER_UNAVAILABLE
        return removePermissionToPlayer(uuid, permission)
    }

    suspend fun removePermissionToPlayer(player: UUID, permission: String): Permissions_removePermissionResult {
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
            plugin.servicesFwk.playerData.putPlayerDataMap(player, pdata)

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






}
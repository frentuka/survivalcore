package site.ftka.survivalcore.services.permissions.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.permissions.PermissionsService
import java.util.UUID

class PermissionsService_PermissionsSubservice(private val service: PermissionsService, private val plugin: MClass) {

    // Yes it DOES include inheritances.
    private val groupPermissionsMap = mutableMapOf<UUID, Set<String>>()
    private val playerPermissionsCache = mutableMapOf<UUID, Set<String>>()

    fun groupHasPerm(groupID: UUID, permission: String) =
        hasPerm(groupPerms(groupID), permission)

    fun playerHasPerm(uuid: UUID, permission: String): Boolean {
        val playerdata = plugin.servicesFwk.playerData.data.getPlayerData(uuid) ?: run {
            playerPermissionsCache.remove(uuid)
            return false
        }

        // if player's permissions are already inside cache, check it
        playerPermissionsCache[uuid]?.let {
            if (hasPerm(it, permission)) return true
        }

        // if not, calculate it
        val playerPerms = playerPerms(uuid)

        // add to cache
        playerPermissionsCache[uuid] = playerPerms

        return hasPerm(playerPerms, permission)
    }

    // cache should be invalidated when a player's permissions change
    // or when a group's permissions change

    // invalidates cache for a player
    fun invalidateCache(playerUUID: UUID) =
        playerPermissionsCache.remove(playerUUID)

    // invalidates cache for all players
    fun invalidateCache() =
        playerPermissionsCache.clear()

    /*
        When is a permission granted? (permission.needed.asd)
        - Has this permission explicitally (e.g. "permission.needed.asd")
        - Has this permission redundantly (e.g. "permission.needed.*")
        - Multiple redundancy (e.g. "permission.*")
    */
    private fun hasPerm(permissions: Set<String>, permission: String): Boolean {
        val permissionSectioned = permission.split(".")

        if (permissions.contains(permission)) return true // explicitally
        for (permissionSection in permissionSectioned) // redundantly
            if (permissions.contains("$permissionSection.*")) return true

        return false
    }

    // returns all permissions of a player
    fun playerPerms(playerUUID: UUID): Set<String> {
        val playerdata = plugin.servicesFwk.playerData.data.getPlayerData(playerUUID) ?: return setOf()
        val groupsUUIDs = playerdata.permissions?.groups ?: setOf()

        val perms = mutableSetOf<String>()
        playerdata.permissions?.let { perms.addAll(it.permissions) }

        for (groupUUID in groupsUUIDs) {
            perms.addAll(groupPerms(groupUUID))
        }

        return perms
    }

    fun groupPerms(groupName: String, includeInheritances: Boolean = true): Set<String> {
        val groupUUID = service.data.getGroup(groupName)?.uuid ?: return setOf()
        return groupPerms(groupUUID, includeInheritances)
    }

    fun groupPerms(groupUUID: UUID, includeInheritances: Boolean = true): Set<String> {
        groupPermissionsMap[groupUUID]?.let { return it }

        val perms = mutableSetOf<String>()
        val group = service.data.getGroup(groupUUID) ?: return perms
        perms.addAll(group.perms)

        if (!includeInheritances) return perms
        for (inh in group.inheritances) {
            val inhGroup = service.data.getGroup(inh)
            inhGroup?.let { perms.addAll(it.perms) }
        }

        groupPermissionsMap[groupUUID] = perms

        return perms
    }

    fun clearCache() {
        groupPermissionsMap.clear()
    }

}
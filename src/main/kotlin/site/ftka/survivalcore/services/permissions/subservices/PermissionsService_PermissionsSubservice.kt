package site.ftka.survivalcore.services.permissions.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.permissions.PermissionsService
import java.util.UUID

class PermissionsService_PermissionsSubservice(private val service: PermissionsService, private val plugin: MClass) {

    // Yes it DOES include inheritances.
    private val groupPermissionsMap = mutableMapOf<UUID, Set<String>>()

    fun groupHasPerm(groupID: UUID, permission: String) =
        hasPerm(groupPerms(groupID), permission)

    fun playerHasPerm(uuid: UUID, permission: String): Boolean {
        val playerdata = plugin.servicesFwk.playerDataService.playerDataMap[uuid] ?: return false
        val playerPerms = mutableSetOf<String>()
        // player's own permissions
        playerPerms.addAll(playerdata.perms.permissions)
        for (groupID in playerdata.perms.groups)
            playerPerms.addAll(groupPerms(groupID))

        return hasPerm(playerPerms, permission)
    }

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

    fun playerPerms(playerUUID: UUID): Set<String> {
        val playerdata = plugin.servicesFwk.playerDataService.playerDataMap[playerUUID] ?: return setOf()
        val groupsUUIDs = playerdata.perms.groups

        val perms = mutableSetOf<String>()

        for (groupUUID in groupsUUIDs) {
            perms.addAll(groupPerms(groupUUID))
        }

        return perms
    }

    fun groupPerms(groupName: String, includeInheritances: Boolean = true): Set<String> {
        val groupUUID = service.groupsNameIDMap[groupName] ?: return setOf()
        return groupPerms(groupUUID, includeInheritances)
    }

    fun groupPerms(groupUUID: UUID, includeInheritances: Boolean = true): Set<String> {
        groupPermissionsMap[groupUUID]?.let { return it }

        val perms = mutableSetOf<String>()
        val group = service.groupsMap[groupUUID] ?: return perms
        perms.addAll(group.perms)

        if (!includeInheritances) return perms
        for (inh in group.inheritances) {
            val inhGroup = service.groupsMap[inh]
            inhGroup?.let { perms.addAll(it.perms) }
        }

        groupPermissionsMap[groupUUID] = perms

        return perms
    }

    fun clearCache() {
        groupPermissionsMap.clear()
    }

}
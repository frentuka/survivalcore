package site.ftka.survivalcore.services.permissions.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.permissions.PermissionsService
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class PermissionsService_PermissionsSubservice(private val service: PermissionsService, private val plugin: MClass) {

    // Yes it DOES include inheritances.
    private val groupPermissionsMap = ConcurrentHashMap<UUID, Set<String>>()
    private val playerPermissionsCache = ConcurrentHashMap<UUID, Set<String>>()

    fun groupHasPerm(groupID: UUID, permission: String) =
        hasPerm(groupPerms(groupID), permission)

    fun playerHasPerm_locally(uuid: UUID, permission: String): Boolean {
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

    fun playerHasPerm(uuid: UUID, permission: String): CompletableFuture<Boolean?> {
        val playerdata = plugin.servicesFwk.playerData.api.getPlayerData(uuid)

        playerPermissionsCache[uuid]?.let { cachedPerms ->
            if (hasPerm(cachedPerms, permission)) return CompletableFuture.completedFuture(true)
        }

        return playerdata?.thenApply {
            if (it == null)
                return@thenApply null

            // if not, calculate it
            val playerPerms = playerPerms(it)

            // add to cache
            playerPermissionsCache[uuid] = playerPerms

            return@thenApply hasPerm(playerPerms, permission)
        } ?: CompletableFuture.completedFuture(null)
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
        if (permissions.contains(permission) || permissions.contains("*")) return true // explicitally or absolute wildcard

        val permissionSectioned = permission.split(".")
        val sb = java.lang.StringBuilder()
        for (i in 0 until permissionSectioned.size - 1) {
            sb.append(permissionSectioned[i]).append(".")
            if (permissions.contains("${sb}*")) return true
        }

        return false
    }

    fun playerPerms(uuid: UUID): Set<String> {
        val playerdata = plugin.servicesFwk.playerData.data.getPlayerData(uuid) ?: return setOf()
        return playerPerms(playerdata)
    }

    // returns all permissions of a player
    fun playerPerms(playerdata: PlayerData): Set<String> {
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
        return calculateGroupPerms(groupUUID, includeInheritances, mutableSetOf())
    }

    private fun calculateGroupPerms(groupUUID: UUID, includeInheritances: Boolean, traversed: MutableSet<UUID>): Set<String> {
        if (traversed.contains(groupUUID)) return emptySet() // Guard circular refs
        traversed.add(groupUUID)

        if (traversed.size == 1) {
            groupPermissionsMap[groupUUID]?.let { return it }
        }

        val perms = mutableSetOf<String>()
        val group = service.data.getGroup(groupUUID) ?: return perms
        perms.addAll(group.perms)

        if (includeInheritances) {
            for (inh in group.inheritances) {
                if (!traversed.contains(inh)) {
                    perms.addAll(calculateGroupPerms(inh, true, traversed))
                }
            }
        }

        if (traversed.size == 1) {
            groupPermissionsMap[groupUUID] = perms
        }

        return perms
    }

    fun clearCache() {
        groupPermissionsMap.clear()
    }

}
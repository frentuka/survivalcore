package site.ftka.survivalcore.services.permissions.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.permissions.PermissionsService
import site.ftka.survivalcore.services.permissions.objects.PermissionGroup
import java.util.*

class PermissionsService_MapsSubservice (private val service: PermissionsService, private val plugin: MClass) {

    private val groupsMap = mutableMapOf<UUID, PermissionGroup>()
    private val groupsNameIDMap = mutableMapOf<String, UUID>()

    fun getGroup(name: String): PermissionGroup? {
        val groupUUID = groupsNameIDMap[name]
        return groupsMap[groupUUID]
    }

    fun getGroup(uuid: UUID): PermissionGroup? {
        return groupsMap[uuid]
    }

    fun getGroups(): Set<PermissionGroup> {
        return groupsMap.values.toSet()
    }

    fun setupGroup(group: PermissionGroup) {
        groupsMap[group.uuid] = group
        groupsNameIDMap[group.name] = group.uuid
    }

    fun clearGroupsMap() {
        groupsMap.clear()
        groupsNameIDMap.clear()
    }

    fun materializeGroup(group: PermissionGroup) {
        // add to cache
        groupsMap[group.uuid] = group
        groupsNameIDMap[group.name] = group.uuid

        // clear caches
        service.permissions_ss.clearCache()
    }

    fun deMaterializeGroup(uuid: UUID) {
        // remove from cache
        groupsMap.remove(uuid)

        // to be removed
        val TBR = mutableSetOf<String>()
        groupsNameIDMap.forEach { (name, uid) ->
            if (uid == uuid)
                TBR.add(name)
        }

        TBR.forEach{ groupsNameIDMap.remove(it) }

        service.permissions_ss.clearCache()
    }

}
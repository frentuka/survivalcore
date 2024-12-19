package site.ftka.survivalcore.services.permissions

import com.google.gson.Gson
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import site.ftka.survivalcore.services.ServicesFramework
import site.ftka.survivalcore.services.permissions.objects.PermissionGroup
import site.ftka.survivalcore.services.permissions.subservices.PermissionsService_GroupsSubservice
import site.ftka.survivalcore.services.permissions.subservices.PermissionsService_InputOutputSubservice
import site.ftka.survivalcore.services.permissions.subservices.PermissionsService_PermissionsSubservice
import site.ftka.survivalcore.services.permissions.subservices.PermissionsService_PlayersSubservice
import java.util.UUID

class PermissionsService(val plugin: MClass, private val services: ServicesFramework) {
    val logger = plugin.loggingInitless.getLog("PermissionsService", Component.text("Perms").color(TextColor.fromHexString("#8298d9")))
    val api = PermissionsAPI(this)

    val permissions_ss      = PermissionsService_PermissionsSubservice(this, plugin)
    val inout_ss            = PermissionsService_InputOutputSubservice(this, plugin)
    val players_ss          = PermissionsService_PlayersSubservice(this, plugin)
    val groups_ss           = PermissionsService_GroupsSubservice(this, plugin)



    private val groupsMap = mutableMapOf<UUID, PermissionGroup>()
    private val groupsNameIDMap = mutableMapOf<String, UUID>()

    fun init() {
        logger.log("Initializing...", LogLevel.LOW)
        inout_ss.storeGroupsIntoMemory(true)

        // create example group
        if (inout_ss.readGroupsFromStorage().isEmpty()) {
            val group = PermissionGroup()
            group.name = "housemaster"
            val asd = setOf("asd.asd1.asd2")
            group.perms = asd

            inout_ss.storeGroupIntoStorage(group)
        }
    }

    fun restart() {
        inout_ss.storeGroupsIntoMemory(true)
    }

    fun stop() {
        logger.log("Stopping...", LogLevel.LOW)

        // save groups
        inout_ss.storeGroupsIntoStorage()
    }

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

    // todo: fix this, it's the same as setupGroup()
    fun materializeGroup(group: PermissionGroup) {
        // add to cache
        groupsMap[group.uuid] = group
        groupsNameIDMap[group.name] = group.uuid

        // clear caches
        permissions_ss.clearCache()
    }

    fun deMaterializeGroup(uuid: UUID) {
        // remove from cache
        groupsMap.remove(uuid)

        val TBR = mutableSetOf<String>()
        groupsNameIDMap.forEach { name, uid ->
            if (uid == uuid)
                TBR.add(name)
        }

        TBR.forEach{ groupsNameIDMap.remove(it) }

        permissions_ss.clearCache()
    }

    // getters

    fun perms(name: String, includeInheritances: Boolean): List<String> {
        val perms = mutableListOf<String>()
        val groupUUID = groupsNameIDMap[name]
        val group = groupsMap[groupUUID] ?: return perms
        perms.addAll(group.perms)

        if (!includeInheritances) return perms
        for (inh in group.inheritances) {
            val inhGroup = groupsMap[inh]
            inhGroup?.let { perms.addAll(it.perms) }
        }

        return perms
    }

    fun fromJson(json: String): PermissionGroup {
        return Gson().fromJson(json, PermissionGroup::class.java)
    }
}
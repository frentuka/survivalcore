package site.ftka.survivalcore.services.permissions

import com.google.gson.Gson
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import site.ftka.survivalcore.services.ServicesFramework
import site.ftka.survivalcore.services.permissions.objects.PermissionGroup
import site.ftka.survivalcore.services.permissions.subservices.PermissionsService_InputOutputSubservice
import site.ftka.survivalcore.services.permissions.subservices.PermissionsService_ModificationsSubservice
import site.ftka.survivalcore.services.permissions.subservices.PermissionsService_PermissionsSubservice
import java.util.UUID

class PermissionsService(val plugin: MClass, private val services: ServicesFramework) {
    val logger = plugin.loggingInitless.getLog("PermissionsService", Component.text("Perms").color(TextColor.fromHexString("#8298d9")))
    val api = PermissionsAPI(this)

    val permissions_ss      = PermissionsService_PermissionsSubservice(this, plugin)
    val modifications_ss    = PermissionsService_ModificationsSubservice(this, plugin)
    val inout_ss            = PermissionsService_InputOutputSubservice(this, plugin)

    val groupsMap = mutableMapOf<UUID, PermissionGroup>()
    val groupsNameIDMap = mutableMapOf<String, UUID>()

    fun init() {
        logger.log("Initializing...", LogLevel.LOW)
        loadGroupsFromStorage()

        // create example group
        if (inout_ss.readGroupsFromStorage().isEmpty()) {
            val group = PermissionGroup()
            group.name = "housemaster"
            val asd = setOf("asd.asd1.asd2")
            group.perms = asd

            inout_ss.saveGroupToStorage(group)
        }
    }

    fun restart() {
        loadGroupsFromStorage()
    }

    private fun loadGroupsFromStorage(clearGroupsMap: Boolean = true) {
        if (clearGroupsMap) { groupsMap.clear(); groupsNameIDMap.clear() }
        for (storedGroup in inout_ss.readGroupsFromStorage()) {
            groupsMap[storedGroup.uuid] = storedGroup
            groupsNameIDMap[storedGroup.name] = storedGroup.uuid
        }
    }

    fun materializeGroup(group: PermissionGroup) {
        // add to cache
        groupsMap[group.uuid] = group
        groupsNameIDMap[group.name] = group.uuid

        // clear caches
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
package site.ftka.survivalcore.services.permissions

import com.google.gson.Gson
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import site.ftka.survivalcore.services.ServicesFramework
import site.ftka.survivalcore.services.permissions.objects.PermissionGroup
import site.ftka.survivalcore.services.permissions.subservices.*

class PermissionsService(val plugin: MClass, private val services: ServicesFramework) {
    internal val logger = plugin.loggingInitless.getLog("PermissionsService", Component.text("Perms").color(TextColor.fromHexString("#8298d9")))

    val api                 = PermissionsAPI(this)
    internal val data                = PermissionsServiceData(this, plugin)

    internal val permissions_ss      = PermissionsService_PermissionsSubservice(this, plugin)
    internal val players_ss          = PermissionsService_PlayersSubservice(this, plugin)
    internal val groups_ss           = PermissionsService_GroupsSubservice(this, plugin)
    internal val inout_ss            = PermissionsService_InputOutputSubservice(this, plugin)

    internal fun init() {
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

    internal fun restart() {
        inout_ss.storeGroupsIntoMemory(true)
    }

    internal fun stop() {
        logger.log("Stopping...", LogLevel.LOW)

        // save groups
        inout_ss.storeGroupsIntoStorage()
    }

    fun fromJson(json: String): PermissionGroup {
        return Gson().fromJson(json, PermissionGroup::class.java)
    }
}
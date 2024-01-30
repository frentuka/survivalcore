package site.ftka.survivalcore.services.permissions.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.logging.LoggingEssential.*
import site.ftka.survivalcore.services.permissions.PermissionsService
import site.ftka.survivalcore.services.permissions.objects.PermissionGroup
import java.util.UUID

class PermissionsService_ModificationsSubservice(private val service: PermissionsService, private val plugin: MClass) {
    private val logger = service.logger.sub("Modif")

    /*
        Group renaming is done here because it's more complex than other modifications
        as the group's name is the same as the file name.
        Other modifications should be done anywhere it's needed and use commitNewGroup() fun
     */

    fun modifyGroupName(groupId: UUID, newName: String) {
        logger.log("Request to modify group name: $groupId", LogLevel.DEBUG)

        // checks
        val group = service.groupsMap[groupId]
        if (group == null) {
            logger.log("Modification failed: group $groupId does not exist.", LogLevel.LOW)
            return }

        // rename local variable
        val oldName = group.name
        group.name = newName

        logger.log("Renaming group '$oldName' to '$newName'")

        // rename file
        service.inout_ss.renameGroupFile(oldName.lowercase(), newName.lowercase())

        // save group into cache
        service.groupsNameIDMap.remove(oldName)

        service.inout_ss.saveGroupToStorage(group)

        service.materializeGroup(group)
    }
}
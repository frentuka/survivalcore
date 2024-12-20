package site.ftka.survivalcore.services.permissions.subservices

import com.sun.net.httpserver.Authenticator.Success
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.permissions.PermissionsService
import site.ftka.survivalcore.services.permissions.objects.PermissionGroup
import java.util.UUID

class PermissionsService_GroupsSubservice(private val service: PermissionsService, private val plugin: MClass) {

    /*
        This subservice will be responsible for managing groups
        (creating, removing and modifying)
     */

    /*
        add group
     */

    /**
     *  | Success -> Returns group
     *  | Group already exists -> Returns null
     */
    fun createGroup(name: String): PermissionGroup? {
        if (service.getGroup(name) != null) return null

        // create group object
        val group = PermissionGroup()

        // save into storage
        service.inout_ss.storeGroupIntoStorage(group)

        // make group available for usage
        service.materializeGroup(group)

        // return new group
        return group
    }

    /*
        remove group
     */

    /**
     *  | True -> Deleted
     *  | False -> Group does not exist
     */
    fun deleteGroup(groupName: String): Boolean {
        service.getGroup(groupName) ?: return false // group does not exist
        service.deMaterializeGroup(service.getGroup(groupName)!!.uuid)
        return service.inout_ss.deleteGroupFile(groupName)
    }

    /*
        modify group
     */

    fun renameGroup(name: String, newName: String) {
        service.getGroup(name) ?: return

        // rename file
        service.inout_ss.renameGroupFile(name, newName)
        service.inout_ss.storeGroupsIntoMemory(true)
    }

    enum class PermissionGroup_modificationResult {
        SUCCESS,
        GROUP_DOES_NOT_EXIST,
        FAILURE_CANT_MODIFY_NAME,
        FAILURE_CANT_MODIFY_UUID
    }

    /**
     * Do NOT modify Group's UUID or NAME.
     * Use renameGroup() instead.
     */
    fun makeModification(uuid: UUID, modification: (PermissionGroup) -> Unit): PermissionGroup_modificationResult {
        val group = service.getGroup(uuid) ?: return PermissionGroup_modificationResult.GROUP_DOES_NOT_EXIST

        val groupsOriginalName = group.name
        modification(group)

        if (group.name != groupsOriginalName) return PermissionGroup_modificationResult.FAILURE_CANT_MODIFY_NAME
        if (group.uuid != uuid) return PermissionGroup_modificationResult.FAILURE_CANT_MODIFY_UUID

        // save changes
        service.inout_ss.storeGroupIntoStorage(group)

        // apply changes
        service.inout_ss.storeGroupsIntoMemory(true)

        return PermissionGroup_modificationResult.SUCCESS
    }

    /*
        add permission to group
     */

    enum class PermissionGroup_addPermissionResult {
        SUCCESS,
        FAILURE_UNKNOWN,
        FAILURE_GROUP_DOES_NOT_EXIST,
        FAILURE_PERMISSION_ALREADY_EXISTS
    }

    fun addPermissionToGroup(name: String, permission: String): PermissionGroup_addPermissionResult {
        service.getGroup(name)?.let{ return addPermissionToGroup(it.uuid, permission) }
        return PermissionGroup_addPermissionResult.FAILURE_GROUP_DOES_NOT_EXIST
    }

    fun addPermissionToGroup(uuid: UUID, permission: String): PermissionGroup_addPermissionResult {
        var result = PermissionGroup_addPermissionResult.SUCCESS

        val mod = makeModification(uuid, {
            val perms = it.perms.toMutableSet()

            if (perms.contains(permission)) {
                result = PermissionGroup_addPermissionResult.FAILURE_PERMISSION_ALREADY_EXISTS
                return@makeModification
            }

            perms.add(permission)
            it.perms = perms.toSet()
        })

        if (result != PermissionGroup_addPermissionResult.SUCCESS)
            return result

        return when (mod) {
            PermissionGroup_modificationResult.SUCCESS -> PermissionGroup_addPermissionResult.SUCCESS
            PermissionGroup_modificationResult.GROUP_DOES_NOT_EXIST -> PermissionGroup_addPermissionResult.FAILURE_GROUP_DOES_NOT_EXIST
            PermissionGroup_modificationResult.FAILURE_CANT_MODIFY_UUID -> PermissionGroup_addPermissionResult.FAILURE_UNKNOWN
            PermissionGroup_modificationResult.FAILURE_CANT_MODIFY_NAME -> PermissionGroup_addPermissionResult.FAILURE_UNKNOWN
        }
    }

    /*
        remove permission to group
     */

    enum class PermissionGroup_removePermissionResult {
        SUCCESS,
        FAILURE_UNKNOWN,
        FAILURE_GROUP_DOES_NOT_EXIST,
        FAILURE_PERMISSION_DOES_NOT_EXIST
    }

    fun removePermissionToGroup(name: String, permission: String): PermissionGroup_removePermissionResult {
        service.getGroup(name)?.let{ return removePermissionToGroup(it.uuid, permission) }
        return PermissionGroup_removePermissionResult.FAILURE_GROUP_DOES_NOT_EXIST
    }

    fun removePermissionToGroup(uuid: UUID, permission: String): PermissionGroup_removePermissionResult {
        var result = PermissionGroup_removePermissionResult.SUCCESS

        val mod = makeModification(uuid, {
            val perms = it.perms.toMutableSet()

            if (!perms.contains(permission)) {
                result = PermissionGroup_removePermissionResult.FAILURE_PERMISSION_DOES_NOT_EXIST
                return@makeModification
            }

            perms.remove(permission)
            it.perms = perms.toSet()
        })

        if (result != PermissionGroup_removePermissionResult.SUCCESS)
            return result

        return when (mod) {
            PermissionGroup_modificationResult.SUCCESS -> PermissionGroup_removePermissionResult.SUCCESS
            PermissionGroup_modificationResult.GROUP_DOES_NOT_EXIST -> PermissionGroup_removePermissionResult.FAILURE_GROUP_DOES_NOT_EXIST
            PermissionGroup_modificationResult.FAILURE_CANT_MODIFY_UUID -> PermissionGroup_removePermissionResult.FAILURE_UNKNOWN
            PermissionGroup_modificationResult.FAILURE_CANT_MODIFY_NAME -> PermissionGroup_removePermissionResult.FAILURE_UNKNOWN
        }
    }

    /*
        add inheritance to group
     */
    enum class PermissionGroup_addInheritanceResult {
        SUCCESS,
        FAILURE_UNKNOWN,
        FAILURE_GROUP_DOES_NOT_EXIST,
        FAILURE_INHERITANCE_ALREADY_SET,
        FAILURE_INHERITANCE_GROUP_DOES_NOT_EXIST
    }

    fun addInheritanceToGroup(name: String, inheritance: String): PermissionGroup_addInheritanceResult {
        val group = service.getGroup(name) ?: return PermissionGroup_addInheritanceResult.FAILURE_GROUP_DOES_NOT_EXIST
        val inhGroup = service.getGroup(inheritance) ?: return PermissionGroup_addInheritanceResult.FAILURE_INHERITANCE_GROUP_DOES_NOT_EXIST
        return addInheritanceToGroup(group.uuid, inhGroup.uuid)
    }

    fun addInheritanceToGroup(uuid: UUID, inheritance: UUID): PermissionGroup_addInheritanceResult {
        service.getGroup(inheritance) ?: return PermissionGroup_addInheritanceResult.FAILURE_INHERITANCE_GROUP_DOES_NOT_EXIST

        var result = PermissionGroup_addInheritanceResult.SUCCESS
        val mod = makeModification(uuid, {
            val inheritances = it.inheritances.toMutableSet()

            if (inheritances.contains(inheritance)) {
                result = PermissionGroup_addInheritanceResult.FAILURE_INHERITANCE_ALREADY_SET
                return@makeModification
            }

            inheritances.add(inheritance)
            it.inheritances = inheritances
        })

        if (result != PermissionGroup_addInheritanceResult.SUCCESS)
            return result

        return when (mod) {
            PermissionGroup_modificationResult.SUCCESS -> PermissionGroup_addInheritanceResult.SUCCESS
            PermissionGroup_modificationResult.GROUP_DOES_NOT_EXIST -> PermissionGroup_addInheritanceResult.FAILURE_GROUP_DOES_NOT_EXIST
            PermissionGroup_modificationResult.FAILURE_CANT_MODIFY_UUID -> PermissionGroup_addInheritanceResult.FAILURE_UNKNOWN
            PermissionGroup_modificationResult.FAILURE_CANT_MODIFY_NAME -> PermissionGroup_addInheritanceResult.FAILURE_UNKNOWN
        }

    }


    /*
        add inheritance to group
     */
    enum class PermissionGroup_removeInheritanceResult {
        SUCCESS,
        FAILURE_UNKNOWN,
        FAILURE_GROUP_DOES_NOT_EXIST,
        FAILURE_INHERITANCE_NOT_IN_GROUP,
        FAILURE_INHERITANCE_GROUP_DOES_NOT_EXIST
    }

    fun removeInheritanceToGroup(name: String, inheritance: String): PermissionGroup_removeInheritanceResult {
        val group = service.getGroup(name) ?: return PermissionGroup_removeInheritanceResult.FAILURE_GROUP_DOES_NOT_EXIST
        val inhGroup = service.getGroup(inheritance) ?: return PermissionGroup_removeInheritanceResult.FAILURE_INHERITANCE_GROUP_DOES_NOT_EXIST
        return removeInheritanceToGroup(group.uuid, inhGroup.uuid)
    }

    fun removeInheritanceToGroup(uuid: UUID, inheritance: UUID): PermissionGroup_removeInheritanceResult {
        var result = PermissionGroup_removeInheritanceResult.SUCCESS
        val mod = makeModification(uuid, {
            val inheritances = it.inheritances.toMutableSet()

            if (!inheritances.contains(inheritance)) {
                result = PermissionGroup_removeInheritanceResult.FAILURE_INHERITANCE_NOT_IN_GROUP
                return@makeModification
            }

            inheritances.add(inheritance)
            it.inheritances = inheritances
        })

        if (result != PermissionGroup_removeInheritanceResult.SUCCESS)
            return result

        return when (mod) {
            PermissionGroup_modificationResult.SUCCESS -> PermissionGroup_removeInheritanceResult.SUCCESS
            PermissionGroup_modificationResult.GROUP_DOES_NOT_EXIST -> PermissionGroup_removeInheritanceResult.FAILURE_GROUP_DOES_NOT_EXIST
            PermissionGroup_modificationResult.FAILURE_CANT_MODIFY_UUID -> PermissionGroup_removeInheritanceResult.FAILURE_UNKNOWN
            PermissionGroup_modificationResult.FAILURE_CANT_MODIFY_NAME -> PermissionGroup_removeInheritanceResult.FAILURE_UNKNOWN
        }
    }

    /*
        modify group's tag
     */
    fun setTagToGroup(uuid: UUID, newTag: String): PermissionGroup_modificationResult {
        return makeModification(uuid, { it.tag = newTag })
    }

    /*
        modify group's primary color
     */
    fun setPrimaryColorToGroup(uuid: UUID, primaryColor: String): PermissionGroup_modificationResult {
        return makeModification(uuid, { it.primaryColor = primaryColor})
    }

    /*
        modify group's secondary color
     */
    fun setSecondaryColorToGroup(uuid: UUID, secondaryColor: String): PermissionGroup_modificationResult {
        return makeModification(uuid, { it.secondaryColor = secondaryColor })
    }

}
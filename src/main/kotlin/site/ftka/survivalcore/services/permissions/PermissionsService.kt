package site.ftka.survivalcore.services.permissions

import com.google.gson.Gson
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.permissions.objects.PermissionGroup
import site.ftka.survivalcore.utils.jsonFileUtils
import java.util.concurrent.ForkJoinPool

class PermissionsService(val plugin: MClass) {
    private val class_log_prefix = "&7[&6Perms&7]"

    private val groupsFolderLocation: String = "${plugin.dataFolder.absolutePath}\\groups"
    private val fjp: ForkJoinPool = ForkJoinPool.commonPool()

    // Almacenamiento
    val groups_cache = mutableMapOf<Int, PermissionGroup>()
    val groups_names = mutableMapOf<String, Int>()

    init {
        reloadGroups()
    }

    // getters

    fun perms(name: String, includeInheritances: Boolean): List<String> { return perms(groups_names[name] ?: return listOf(), includeInheritances) }
    fun perms(id: Int, includeInheritances: Boolean): List<String> {
        val perms = mutableListOf<String>()
        val group = groups_cache[id] ?: return perms
        perms.addAll(group.perms)

        if (!includeInheritances) return perms
        for (inh in group.inheritances) {
            val inhgroup = groups_cache[id] ?: continue
            perms.addAll(inhgroup.perms)
        }

        return perms
    }

    /*
        storage
     */

    fun reloadGroups() {
        fjp.submit {
            groups_cache.clear()
            groups_names.clear()
            readGroupsFromStorage().forEach{
                groups_cache[it.id] = it
                groups_names[it.name] = it.id
            }
        }
    }

    private fun readGroupsFromStorage(): List<PermissionGroup> {
        val groups = mutableListOf<PermissionGroup>()
        jsonFileUtils.readAllJson(groupsFolderLocation).forEach{ groups.add(fromJson(it)) }
        return groups
    }

    fun saveGroupToStorage(group: PermissionGroup) {
        fjp.submit { jsonFileUtils.saveJson(groupsFolderLocation, "${group.name}.json", group.toJson()) }
    }

    private fun fromJson(json: String): PermissionGroup {
        return Gson().fromJson(json, PermissionGroup::class.java)
    }
}
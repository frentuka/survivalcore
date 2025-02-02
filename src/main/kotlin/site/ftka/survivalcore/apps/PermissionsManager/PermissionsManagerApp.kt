package site.ftka.survivalcore.apps.PermissionsManager

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.PermissionsManager.commands.PermissionsManagerApp_GroupsCommand
import site.ftka.survivalcore.apps.PermissionsManager.commands.PermissionsManagerApp_PermsCommand

class PermissionsManagerApp(val plugin: MClass) {

    private val permsCommand = PermissionsManagerApp_PermsCommand(this, plugin)
    private val groupsCommand = PermissionsManagerApp_GroupsCommand(this, plugin)

    internal fun init() {
        // init command

        // perms command is player-side
        plugin.getCommand("p")?.setExecutor(permsCommand)
        plugin.getCommand("perm")?.setExecutor(permsCommand)
        plugin.getCommand("perms")?.setExecutor(permsCommand)
        plugin.getCommand("permissions")?.setExecutor(permsCommand)

        // groups command is group-side
        plugin.getCommand("g")?.setExecutor(groupsCommand)
        plugin.getCommand("group")?.setExecutor(groupsCommand)
        plugin.getCommand("groups")?.setExecutor(groupsCommand)
    }

    internal fun restart() {
        // nothing to restart
    }

    internal fun stop() {
        // nothing to stop
    }

}
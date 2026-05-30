package site.ftka.survivalcore.apps.PermissionsManager

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.PermissionsManager.commands.PermissionsManagerApp_GroupsCommand
import site.ftka.survivalcore.apps.PermissionsManager.commands.PermissionsManagerApp_PermsCommand

class PermissionsManagerApp(val plugin: MClass) {

    private val permsCommand = PermissionsManagerApp_PermsCommand(this, plugin)
    private val groupsCommand = PermissionsManagerApp_GroupsCommand(this, plugin)

    internal fun init() {
        // init commands
        plugin.getCommand("permissions")?.setExecutor(permsCommand)
        plugin.getCommand("groups")?.setExecutor(groupsCommand)
    }

    internal fun restart() {
        // nothing to restart
    }

    internal fun stop() {
        // nothing to stop
    }

}
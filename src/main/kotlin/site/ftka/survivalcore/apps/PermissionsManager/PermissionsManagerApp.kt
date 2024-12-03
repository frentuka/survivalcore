package site.ftka.survivalcore.apps.PermissionsManager

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.PermissionsManager.commands.PermissionsManagerApp_Command

class PermissionsManagerApp(val plugin: MClass) {

    private val permissionsCommand = PermissionsManagerApp_Command(this, plugin)

    fun init() {

        // init command
        plugin.getCommand("p")?.setExecutor(permissionsCommand)
        plugin.getCommand("perms")?.setExecutor(permissionsCommand)
        plugin.getCommand("permissions")?.setExecutor(permissionsCommand)
    }

    fun restart() {
        // nothing to restart
    }

    fun stop() {
        // nothing to stop
    }

}
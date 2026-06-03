package site.ftka.survivalcore.apps.PermissionsManager

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.PermissionsManager.commands.PermissionsManagerApp_GroupsCommand
import site.ftka.survivalcore.apps.PermissionsManager.commands.PermissionsManagerApp_PermsCommand
import site.ftka.survivalcore.apps.PermissionsManager.gui.PermissionsManager_ChatInputInterceptor

class PermissionsManagerApp(val plugin: MClass) {

    val chatInterceptor = PermissionsManager_ChatInputInterceptor(plugin)

    private val permsCommand = PermissionsManagerApp_PermsCommand(this, plugin)
    private val groupsCommand = PermissionsManagerApp_GroupsCommand(this, plugin)

    internal fun init() {
        // init commands
        plugin.getCommand("permissions")?.apply {
            setExecutor(permsCommand)
            setTabCompleter(permsCommand)
        }
        plugin.getCommand("groups")?.apply {
            setExecutor(groupsCommand)
            setTabCompleter(groupsCommand)
        }
        
        // register chat interceptor listener
        plugin.initListener(chatInterceptor)
    }

    internal fun restart() {
        // nothing to restart
    }

    internal fun stop() {
        // nothing to stop
    }

}
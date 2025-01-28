package site.ftka.survivalcore.apps.ServerAdministration

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.ServerAdministration.commands.ServerAdministration_serverCommand
import site.ftka.survivalcore.apps.ServerAdministration.lang.ServerAdministration_CommandLang

class ServerAdministrationApp(private val plugin: MClass) {

    /*
        Will provide commands and a GUI
        for internal control of services and essentials

     */
    private val serverCommand = ServerAdministration_serverCommand(this, plugin)
    internal val lang = ServerAdministration_CommandLang()

    internal fun init() {
        // init commands
        plugin.getCommand("server")?.setExecutor(serverCommand)
        plugin.server.pluginManager.registerEvents(serverCommand, plugin)
    }

    internal fun restart() {
        // nothing to restart
    }

    internal fun stop() {
        // nothing to stop
    }

}
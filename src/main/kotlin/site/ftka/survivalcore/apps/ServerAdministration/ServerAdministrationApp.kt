package site.ftka.survivalcore.apps.ServerAdministration

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.ServerAdministration.commands.ServerAdministration_serverCommand

class ServerAdministrationApp(private val plugin: MClass) {

    /*
        Will provide commands and a GUI
        for internal control of services and essentials

     */
    private val serverCommand = ServerAdministration_serverCommand(plugin)

    fun init() {
        // init commands
        plugin.getCommand("server")?.setExecutor(serverCommand)
    }

    fun restart() {

    }

}
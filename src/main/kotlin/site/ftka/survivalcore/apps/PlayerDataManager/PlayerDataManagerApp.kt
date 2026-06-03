package site.ftka.survivalcore.apps.PlayerDataManager

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.PlayerDataManager.commands.PlayerDataManager_Command

class PlayerDataManagerApp(val plugin: MClass) {

    private val command = PlayerDataManager_Command(this, plugin)

    internal fun init() {
        plugin.getCommand("playerdata")?.setExecutor(command)
    }

    internal fun restart() {}

    internal fun stop() {}
}

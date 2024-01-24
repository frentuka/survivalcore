package site.ftka.survivalcore.services.playerdata.listeners

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import site.ftka.survivalcore.MClass
import java.io.File

class WorldPlayerDataDeleter(private val plugin: MClass): Listener {

    /*
    This class is meant to control (delete)
    the world's PlayerData folder
    so that it does not exist

    It's purpose is to leave full control of the playerdata
    to the playerdata service.
    */

    init {
        deletePlayerDataFolder(100)
    }

    // delete playerdata folder after every logout
    @EventHandler
    fun deletePlayerDataOnPlayerQuit(event: PlayerQuitEvent) {
        deletePlayerDataFolder(500)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun deletePlayerDataFolder(delay: Long) {
        GlobalScope.launch {
            delay(delay)
            val worldFolder = plugin.server.worldContainer.absolutePath.replace(".", "world")
            val playerdataFolder = "$worldFolder\\playerdata"
            val playerdataFolderFile = File(playerdataFolder)

            if (playerdataFolderFile.exists() && playerdataFolderFile.isDirectory && playerdataFolderFile.listFiles() != null)
                playerdataFolderFile.listFiles()?.forEach { it.deleteRecursively() }
        }
    }

}
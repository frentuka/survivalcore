package site.ftka.survivalcore.services

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chunkborder.ChunkBorderService
import site.ftka.survivalcore.services.inventorygui.InventoryGUIService
import site.ftka.survivalcore.services.language.LanguageService
import site.ftka.survivalcore.services.permissions.PermissionsService
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.singula.SingulaService
import site.ftka.survivalcore.services.worldboard.WorldBoardService
import site.ftka.survivalcore.services.territory.TerritoryService
import site.ftka.survivalcore.services.spawnfinder.SpawnFinderService
import site.ftka.survivalcore.services.gameplay.GameplayService

class ServicesFramework(private val plugin: MClass) {
    private val logger = plugin.loggingInitless.getLog("ServicesFramework", Component.text("Services").color(NamedTextColor.RED))

    var playerData      = PlayerDataService(plugin, this)
    var language        = LanguageService(plugin, this)
    var permissions     = PermissionsService(plugin, this)
    var inventoryGUI    = InventoryGUIService(plugin, this)
    var worldBoard      = WorldBoardService(plugin, this)
    var chunkBorder     = ChunkBorderService(plugin, this)
    var territory       = TerritoryService(plugin, this)
    var spawnFinder     = SpawnFinderService(plugin, this)
    var gameplay        = GameplayService(plugin, this)
    var singula         = SingulaService(plugin, this)

    fun initAll() {
        logger.log("Initializing services...")

        playerData.init()
        language.init()
        permissions.init()
        inventoryGUI.init()
        worldBoard.init()
        chunkBorder.init()
        territory.init()
        spawnFinder.init()
        gameplay.init()

        singula.init()
    }

    fun restartAll() {
        logger.log("Restarting services...")

        playerData.restart()
        language.restart()
        permissions.restart()
        inventoryGUI.restart()
        worldBoard.restart()
        chunkBorder.restart()
        territory.restart()
        spawnFinder.restart()
        gameplay.restart()

        singula.restart()
    }

    fun stopAll() {
        logger.log("Stopping services...")

        playerData.stop()
        language.stop()
        permissions.stop()
        inventoryGUI.stop()
        worldBoard.stop()
        chunkBorder.stop()
        territory.stop()
        spawnFinder.stop()
        gameplay.stop()

        singula.stop()
    }
}
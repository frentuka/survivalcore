package site.ftka.survivalcore.services

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.inventorygui.InventoryGUIService
import site.ftka.survivalcore.services.language.LanguageService
import site.ftka.survivalcore.services.permissions.PermissionsService
import site.ftka.survivalcore.services.playerdata.PlayerDataService

class ServicesFramework(private val plugin: MClass) {
    private val logger = plugin.loggingInitless.getLog("ServicesFramework", Component.text("Services").color(NamedTextColor.RED))

    var playerDataService = PlayerDataService(plugin, this)
    var languageService = LanguageService(plugin, this)
    var permissionsService = PermissionsService(plugin, this)
    var inventoryGUIService = InventoryGUIService(plugin, this)

    fun initAll() {
        logger.log("Initializing services...")

        playerDataService.init()
        languageService.init()
        permissionsService.init()
        inventoryGUIService.init()
    }

    fun restartAll() {
        logger.log("Restarting services...")

        playerDataService.restart()
        languageService.restart()
        permissionsService.restart()
        inventoryGUIService.restart()
    }

}
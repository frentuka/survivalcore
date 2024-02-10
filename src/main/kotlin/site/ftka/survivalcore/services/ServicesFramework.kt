package site.ftka.survivalcore.services

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.inventorygui.InventoryGUIService
import site.ftka.survivalcore.services.language.LanguageService
import site.ftka.survivalcore.services.permissions.PermissionsService
import site.ftka.survivalcore.services.playerdata.PlayerDataService

class ServicesFramework(private val plugin: MClass) {

    var playerDataService = PlayerDataService(plugin, this)
    var languageService = LanguageService(plugin, this)
    var permissionsService = PermissionsService(plugin, this)
    var inventoryGUIService = InventoryGUIService(plugin, this)

    fun initAll() {
        playerDataService.init()
        languageService.init()
        permissionsService.init()
        inventoryGUIService.init()
    }

    fun restartAll() {
        playerDataService.restart()
        languageService.restart()
        permissionsService.restart()
        inventoryGUIService.restart()
    }

}
package site.ftka.survivalcore.services

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chat.ChatService
import site.ftka.survivalcore.services.inventorygui.InventoryGUIService
import site.ftka.survivalcore.services.language.LanguageService
import site.ftka.survivalcore.services.permissions.PermissionsService
import site.ftka.survivalcore.services.playerdata.PlayerDataService

class ServicesFramework(private val plugin: MClass) {
    private val logger = plugin.loggingInitless.getLog("ServicesFramework", Component.text("Services").color(NamedTextColor.RED))

    var chat            = ChatService(plugin, this)
    var playerData      = PlayerDataService(plugin, this)
    var language        = LanguageService(plugin, this)
    var permissions     = PermissionsService(plugin, this)
    var inventoryGUI    = InventoryGUIService(plugin, this)

    fun initAll() {
        logger.log("Initializing services...")

        chat.init()
        playerData.init()
        language.init()
        permissions.init()
        inventoryGUI.init()
    }

    fun restartAll() {
        logger.log("Restarting services...")

        chat.restart()
        playerData.restart()
        language.restart()
        permissions.restart()
        inventoryGUI.restart()
    }

    fun stopAll() {
        logger.log("Stopping services...")

        chat.stop()
        playerData.stop()
        language.stop()
        permissions.stop()
        inventoryGUI.stop()
    }
}
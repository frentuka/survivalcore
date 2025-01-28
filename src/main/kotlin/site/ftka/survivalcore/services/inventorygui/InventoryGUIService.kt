package site.ftka.survivalcore.services.inventorygui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import site.ftka.survivalcore.services.ServicesFramework
import site.ftka.survivalcore.services.inventorygui.events.InventoryGUIInitEvent
import site.ftka.survivalcore.services.inventorygui.events.InventoryGUIRestartEvent
import site.ftka.survivalcore.services.inventorygui.interfaces.InventoryGUIOwner
import site.ftka.survivalcore.services.inventorygui.listeners.InventoryGUIDetectionListener

class InventoryGUIService(private val plugin: MClass, private val services: ServicesFramework) {
    internal val logger = plugin.loggingInitless.getLog("InventoryGUI", Component.text("InvGUI").color(TextColor.fromHexString("#cc6600")))
    val api = InventoryGUIAPI(this)

    /*
        This service is meant to control
        inventory gui interfaces.

        Modus operandi:
        - Use InventoryGUI object to create a new inventory and display it
        - ...
     */

    private val detectionListener = InventoryGUIDetectionListener(this)

    private val inventoryOwnersMap = mutableMapOf<String, InventoryGUIOwner>()

    internal fun init() {
        logger.log("Initializing...", LogLevel.LOW)
        plugin.initListener(detectionListener)

        val event = InventoryGUIInitEvent()
        plugin.propEventsInitless.fireEvent(event)
    }

    internal fun restart() {
        logger.log("Restarting...", LogLevel.LOW)
        val event = InventoryGUIRestartEvent()
        plugin.propEventsInitless.fireEvent(event)
    }

    internal fun stop() {
        logger.log("Stopping...", LogLevel.LOW)
    }

    fun createInventory(owner: InventoryGUIOwner, type: InventoryType, title: Component): Inventory {
        inventoryOwnersMap[owner.ownerName] = owner
        val inv = Bukkit.createInventory(owner, type, title)
        return Bukkit.createInventory(owner, type, title)
    }

    fun getInventoryOwner(ownerName: String): InventoryGUIOwner?
    = inventoryOwnersMap[ownerName]
}
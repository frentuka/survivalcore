package site.ftka.survivalcore.services.inventorygui.interfaces

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.InventoryHolder

interface InventoryGUIOwner: InventoryHolder {

    val ownerName: String

    fun openEvent(event: InventoryOpenEvent)
    fun closeEvent(event: InventoryCloseEvent)

    fun clickEvent(event: InventoryClickEvent)

}
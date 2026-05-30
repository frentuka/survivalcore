package site.ftka.survivalcore.services.inventorygui.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import site.ftka.survivalcore.services.inventorygui.InventoryGUIService
import site.ftka.survivalcore.services.inventorygui.interfaces.InventoryGUIOwner

class InventoryGUIDetectionListener(private val service: InventoryGUIService) : Listener {

    @EventHandler
    fun inventoryOpen(event: InventoryOpenEvent) {
        detectInventoryOwner(event.inventory)?.openEvent(event)
    }

    @EventHandler
    fun inventoryClose(event: InventoryCloseEvent) {
        detectInventoryOwner(event.inventory)?.closeEvent(event)
    }

    @EventHandler
    fun inventoryClick(event: InventoryClickEvent) {
        detectInventoryOwner(event.inventory)?.clickEvent(event)
    }

    @EventHandler
    fun inventoryDrag(event: InventoryDragEvent) {
        detectInventoryOwner(event.inventory)?.dragEvent(event)
    }

    private fun detectInventoryOwner(inventory: Inventory): InventoryGUIOwner? {
        return inventory.holder as? InventoryGUIOwner
    }

}
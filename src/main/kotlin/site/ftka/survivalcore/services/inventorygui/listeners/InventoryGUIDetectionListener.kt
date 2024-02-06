package site.ftka.survivalcore.services.inventorygui.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import site.ftka.survivalcore.services.inventorygui.InventoryGUIService
import site.ftka.survivalcore.services.inventorygui.interfaces.InventoryGUIOwner

class InventoryGUIDetectionListener(private val service: InventoryGUIService): Listener {

    @EventHandler
    fun inventoryOpen(event: InventoryOpenEvent) =
        detectInventoryOwner(event.inventory)?.openEvent(event)

    @EventHandler
    fun inventoryClose(event: InventoryCloseEvent) =
        detectInventoryOwner(event.inventory)?.closeEvent(event)

    @EventHandler
    fun inventoryInteract(event: InventoryInteractEvent) =
        detectInventoryOwner(event.inventory)?.interactEvent(event)

    @EventHandler
    fun inventoryClick(event: InventoryClickEvent) =
        detectInventoryOwner(event.inventory)?.clickEvent(event)

    private fun detectInventoryOwner(inventory: Inventory): InventoryGUIOwner? {
        val invHolder = inventory.holder
        if (invHolder !is InventoryGUIOwner) return null

        val ownerName = invHolder.ownerName
        return service.getInventoryOwner(ownerName)
    }

}
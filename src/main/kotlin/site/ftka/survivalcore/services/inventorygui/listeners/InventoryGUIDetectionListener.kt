package site.ftka.survivalcore.services.inventorygui.listeners

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
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
        val anvil = service.activeAnvilInputs[event.player.uniqueId]
        if (anvil != null) {
            anvil.handleClose(event)
            return
        }
        detectInventoryOwner(event.inventory)?.closeEvent(event)
    }

    @EventHandler
    fun inventoryClick(event: InventoryClickEvent) {
        val anvil = service.activeAnvilInputs[event.whoClicked.uniqueId]
        if (anvil != null) {
            anvil.handleClick(event)
            return
        }
        detectInventoryOwner(event.inventory)?.clickEvent(event)
    }

    @EventHandler
    fun inventoryDrag(event: InventoryDragEvent) {
        val anvil = service.activeAnvilInputs[event.whoClicked.uniqueId]
        if (anvil != null) {
            anvil.handleDrag(event)
            return
        }
        detectInventoryOwner(event.inventory)?.dragEvent(event)
    }

    @EventHandler
    fun prepareAnvil(event: PrepareAnvilEvent) {
        val viewer = event.inventory.viewers.firstOrNull() as? Player ?: return
        val anvil = service.activeAnvilInputs[viewer.uniqueId]
        if (anvil != null) {
            anvil.handlePrepareAnvil(event)
        }
    }

    private fun detectInventoryOwner(inventory: Inventory): InventoryGUIOwner? {
        return inventory.holder as? InventoryGUIOwner
    }

}
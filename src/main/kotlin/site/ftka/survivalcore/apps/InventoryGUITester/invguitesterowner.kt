package site.ftka.survivalcore.apps.InventoryGUITester

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import site.ftka.survivalcore.services.inventorygui.interfaces.InventoryGUIOwner
import javax.naming.ServiceUnavailableException

class invguitesterowner: InventoryGUIOwner {
    override val ownerName = "InvGUITester"

    override fun openEvent(event: InventoryOpenEvent) {
        println("OPEN EVENT")
    }

    override fun closeEvent(event: InventoryCloseEvent) {
        println("CLOSE EVENT")
    }

    override fun interactEvent(event: InventoryInteractEvent) {
        println("INTERACT EVENT")
    }

    override fun clickEvent(event: InventoryClickEvent) {
        println("CLICK EVENT")
    }

    override fun getInventory(): Inventory {
        throw ServiceUnavailableException("getInventory() is not allowed here.")
    }
}
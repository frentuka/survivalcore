package site.ftka.survivalcore.apps.InventoryGUITester

import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.event.inventory.*
import org.bukkit.inventory.Inventory
import site.ftka.survivalcore.services.inventorygui.interfaces.InventoryGUIOwner
import javax.naming.ServiceUnavailableException

class invguitesterowner: InventoryGUIOwner {
    override val ownerName = "InvGUITester"

    override fun openEvent(event: InventoryOpenEvent) {
    }

    override fun closeEvent(event: InventoryCloseEvent) {
    }

    override fun clickEvent(event: InventoryClickEvent) {
        if (event.slot == 10) {
            event.isCancelled = true
            if (event.click == ClickType.SHIFT_RIGHT)
                event.whoClicked.showTitle(Title.title(Component.text("XDD"), Component.text("XDDDDDDDDDDDDDDDDDDDDDDD")))
        }
    }

    override fun getInventory(): Inventory {
        throw ServiceUnavailableException("getInventory() is not allowed here.")
    }
}
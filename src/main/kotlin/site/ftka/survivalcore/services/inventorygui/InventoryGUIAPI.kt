package site.ftka.survivalcore.services.inventorygui

import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.InventoryType
import site.ftka.survivalcore.services.inventorygui.interfaces.InventoryGUIOwner

class InventoryGUIAPI(private val svc: InventoryGUIService) {

    fun createInventory(owner: InventoryGUIOwner, type: InventoryType, title: Component)
        = svc.createInventory(owner, type, title)

    fun getInventoryOwner(ownerName: String)
        = svc.getInventoryOwner(ownerName)

}
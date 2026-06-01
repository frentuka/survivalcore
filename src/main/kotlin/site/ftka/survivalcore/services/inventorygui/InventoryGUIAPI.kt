package site.ftka.survivalcore.services.inventorygui

import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import site.ftka.survivalcore.services.inventorygui.interfaces.InventoryGUIOwner

class InventoryGUIAPI(private val svc: InventoryGUIService) {

    fun createInventory(owner: InventoryGUIOwner, type: InventoryType, title: Component): Inventory
        = svc.createInventory(owner, type, title)

    fun createInventory(owner: InventoryGUIOwner, size: Int, title: Component): Inventory
        = svc.createInventory(owner, size, title)

    fun openAnvilInput(
        player: org.bukkit.entity.Player,
        title: Component,
        placeholderText: String = "Enter text...",
        isHexColor: Boolean = false,
        callback: (String) -> Unit
    ) = svc.openAnvilInput(player, title, placeholderText, isHexColor, callback)

}
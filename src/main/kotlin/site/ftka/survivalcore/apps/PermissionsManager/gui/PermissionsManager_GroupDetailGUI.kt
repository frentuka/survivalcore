package site.ftka.survivalcore.apps.PermissionsManager.gui

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.inventorygui.interfaces.InventoryGUIOwner
import java.util.UUID

internal class PermissionsManager_GroupDetailGUI(
    private val plugin: MClass,
    private val player: Player,
    private val groupUUID: UUID
) : InventoryGUIOwner {

    override val ownerName = "PermissionsManager_GroupDetail_${player.uniqueId}"
    private val inv: Inventory
    private val mm = MiniMessage.miniMessage()
    private val permsAPI = plugin.servicesFwk.permissions.api

    init {
        val group = permsAPI.getGroup(groupUUID)
        val name = group?.displayName ?: "Group"
        val title = mm.deserialize("<#8298d9><bold>$name Core Config</bold></#8298d9>")
        inv = plugin.servicesFwk.inventoryGUI.api.createInventory(this, 27, title)
        setupItems()
    }

    private fun createItem(material: Material, name: String, vararg lore: String): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item
        meta.displayName(mm.deserialize(name))
        meta.lore(lore.map { mm.deserialize(it) })
        item.itemMeta = meta
        return item
    }

    private fun setupItems() {
        inv.clear()

        val filler = createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray> </gray>")
        for (i in 0 until 27) {
            if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, filler)
            }
        }

        val group = permsAPI.getGroup(groupUUID)
        if (group == null) {
            inv.setItem(13, createItem(Material.BARRIER, "<red>Group not found</red>"))
            return
        }

        // Info Book
        val infoItem = createItem(
            Material.BOOK,
            "<${group.primaryColor}><bold>${group.displayName} Core Info</bold></${group.primaryColor}>",
            "<gray>UUID: <white>${group.uuid}</white></gray>",
            "<gray>Name ID: <white>${group.name}</white></gray>",
            "<gray>Category: <white>${group.category.name}</white></gray>",
            "",
            "<yellow>Configuration:</yellow>",
            "<gray>This menu contains practical server settings.</gray>",
            "<gray>Visuals can be configured separately.</gray>"
        )
        inv.setItem(10, infoItem)

        // Permissions Editor
        val permsItem = createItem(
            Material.WRITABLE_BOOK,
            "<#ff9900><b>Manage Permissions</b></#ff9900>",
            "<gray>List, add, or delete permissions</gray>",
            "<gray>directly assigned to this group.</gray>",
            "",
            "<gray>Active Direct Perms: <white>${group.perms.size}</white></gray>",
            "",
            "<yellow>Click to view permissions</yellow>"
        )
        inv.setItem(12, permsItem)

        // Inheritances Editor
        val inheritanceItem = createItem(
            Material.SHIELD,
            "<#a0ac3a><b>Manage Inheritances</b></#a0ac3a>",
            "<gray>Enable or disable parent groups</gray>",
            "<gray>that this group inherits from.</gray>",
            "",
            "<gray>Inheriting: <white>${group.inheritances.size} groups</white></gray>",
            "",
            "<yellow>Click to configure inheritances</yellow>"
        )
        inv.setItem(14, inheritanceItem)

        // Visuals Settings Page Redirect
        val visualsItem = createItem(
            Material.PAINTING,
            "<#55ffff><b>Edit Visual Properties</b></#55ffff>",
            "<gray>Configure chat tags, primary</gray>",
            "<gray>colors, and secondary colors.</gray>",
            "",
            "<yellow>Click to open visual editor</yellow>"
        )
        inv.setItem(16, visualsItem)

        // Delete Button (Placed in the bottom row center for safety)
        val deleteBtn = createItem(
            Material.TNT,
            "<red><bold>DELETE GROUP</bold></red>",
            "<gray>Completely delete this group</gray>",
            "<gray>from the server registry.</gray>",
            "",
            "<red>⚠️ DANGER ZONE ⚠️</red>"
        )
        inv.setItem(22, deleteBtn)

        // Back Button
        val backBtn = createItem(Material.ARROW, "<red>Back to Groups</red>")
        inv.setItem(18, backBtn)
    }

    override fun getInventory(): Inventory = inv

    override fun clickEvent(event: InventoryClickEvent) {
        val clickedInv = event.clickedInventory ?: return
        if (clickedInv == inv) {
            event.isCancelled = true

            when (event.slot) {
                12 -> {
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        val gui = PermissionsManager_GroupPermsGUI(plugin, player, groupUUID)
                        player.openInventory(gui.inventory)
                    }, null, 0L)
                }
                14 -> {
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        val gui = PermissionsManager_GroupInheritanceGUI(plugin, player, groupUUID)
                        player.openInventory(gui.inventory)
                    }, null, 0L)
                }
                16 -> {
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        val gui = PermissionsManager_GroupVisualsGUI(plugin, player, groupUUID)
                        player.openInventory(gui.inventory)
                    }, null, 0L)
                }
                18 -> {
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        val gui = PermissionsManager_GroupsListGUI(plugin, player)
                        player.openInventory(gui.inventory)
                    }, null, 0L)
                }
                22 -> {
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        val gui = PermissionsManager_GroupDeleteConfirmGUI(plugin, player, groupUUID)
                        player.openInventory(gui.inventory)
                    }, null, 0L)
                }
            }
        }
    }

    override fun dragEvent(event: InventoryDragEvent) {
        if (event.rawSlots.any { it < inv.size }) {
            event.isCancelled = true
        }
    }
}

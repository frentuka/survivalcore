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

internal class PermissionsManager_GroupDeleteConfirmGUI(
    private val plugin: MClass,
    private val player: Player,
    private val groupUUID: UUID
) : InventoryGUIOwner {

    override val ownerName = "PermissionsManager_GroupDeleteConfirm_${player.uniqueId}"
    private val inv: Inventory
    private val mm = MiniMessage.miniMessage()
    private val permsAPI = plugin.servicesFwk.permissions.api

    init {
        val group = permsAPI.getGroup(groupUUID)
        val name = group?.displayName ?: "Group"
        val title = mm.deserialize("<red><bold>Confirm Deleting $name?</bold></red>")
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

        // Cancel Buttons (Red Concrete)
        val cancelItem = createItem(
            Material.RED_CONCRETE,
            "<red><b>✖ Cancel Deletion</b></red>",
            "<gray>Safely abort this action and</gray>",
            "<gray>return to group details.</gray>"
        )
        inv.setItem(10, cancelItem)
        inv.setItem(11, cancelItem)
        inv.setItem(12, cancelItem)

        // Warning Info Book
        val warningItem = createItem(
            Material.BOOK,
            "<red><bold>⚠️ DANGER: PERMANENT DELETION ⚠️</bold></red>",
            "<gray>You are about to permanently delete</gray>",
            "<gray>the permission group <white>${group.displayName}</white>.</gray>",
            "",
            "<red>This action CANNOT be undone!</red>",
            "<gray>All members will lose this group,</gray>",
            "<gray>and inheritances will be broken.</gray>"
        )
        inv.setItem(13, warningItem)

        // Confirm Buttons (Green Concrete)
        val confirmItem = createItem(
            Material.GREEN_CONCRETE,
            "<green><b>✔ Confirm Deletion</b></green>",
            "<gray>Permanently delete this group</gray>",
            "<gray>and all its server files.</gray>"
        )
        inv.setItem(14, confirmItem)
        inv.setItem(15, confirmItem)
        inv.setItem(16, confirmItem)
    }

    override fun getInventory(): Inventory = inv

    override fun clickEvent(event: InventoryClickEvent) {
        val clickedInv = event.clickedInventory ?: return
        if (clickedInv == inv) {
            event.isCancelled = true

            val group = permsAPI.getGroup(groupUUID) ?: return

            when (event.slot) {
                10, 11, 12 -> {
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        val gui = PermissionsManager_GroupDetailGUI(plugin, player, groupUUID)
                        player.openInventory(gui.inventory)
                    }, null, 0L)
                }
                14, 15, 16 -> {
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        val success = permsAPI.deleteGroup(group.name)
                        if (success) {
                            plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<green>✔ Group '${group.name}' has been successfully deleted.</green>"))
                        } else {
                            plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<red>✖ Failed to delete group '${group.name}'.</red>"))
                        }
                        val gui = PermissionsManager_GroupsListGUI(plugin, player)
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

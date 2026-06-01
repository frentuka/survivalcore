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

internal class PermissionsManager_GroupPermsGUI(
    private val plugin: MClass,
    private val player: Player,
    private val groupUUID: UUID,
    private var page: Int = 0
) : InventoryGUIOwner {

    override val ownerName = "PermissionsManager_GroupPerms_${player.uniqueId}"
    private val inv: Inventory
    private val mm = MiniMessage.miniMessage()
    private val permsAPI = plugin.servicesFwk.permissions.api
    private var permissionsList = listOf<String>()

    private val contentSlots = listOf(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    )

    init {
        val group = permsAPI.getGroup(groupUUID)
        val name = group?.displayName ?: "Group"
        val title = mm.deserialize("<#8298d9><bold>$name Perms (P. ${page + 1})</bold></#8298d9>")
        inv = plugin.servicesFwk.inventoryGUI.api.createInventory(this, 54, title)
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
        for (i in 0 until 54) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, filler)
            }
        }

        val group = permsAPI.getGroup(groupUUID)
        if (group == null) {
            inv.setItem(22, createItem(Material.BARRIER, "<red>Group not found</red>"))
            return
        }

        permissionsList = group.perms.sorted()
        val startIndex = page * 28
        val endIndex = minOf(startIndex + 28, permissionsList.size)

        for (i in startIndex until endIndex) {
            val perm = permissionsList[i]
            val slot = contentSlots[i - startIndex]
            
            val permItem = createItem(
                Material.PAPER,
                "<yellow>$perm</yellow>",
                "<gray>Direct permission node.</gray>",
                "",
                "<red>Click to delete from group</red>"
            )
            inv.setItem(slot, permItem)
        }

        // Add Permission button
        val addBtn = createItem(
            Material.ANVIL,
            "<green><bold>+ Add Permission</bold></green>",
            "<gray>Click to prompt a command suggestion</gray>",
            "<gray>to add a permission to this group.</gray>"
        )
        inv.setItem(49, addBtn)

        // Pagination buttons
        if (page > 0) {
            inv.setItem(48, createItem(Material.ARROW, "<gold>← Previous Page</gold>"))
        }
        if (endIndex < permissionsList.size) {
            inv.setItem(50, createItem(Material.ARROW, "<gold>Next Page →</gold>"))
        }

        // Back button
        inv.setItem(45, createItem(Material.ARROW, "<red>Back to Details</red>"))
    }

    override fun getInventory(): Inventory = inv

    override fun clickEvent(event: InventoryClickEvent) {
        val clickedInv = event.clickedInventory ?: return
        if (clickedInv == inv) {
            event.isCancelled = true

            val group = permsAPI.getGroup(groupUUID) ?: return
            val slot = event.slot

            if (slot == 45) {
                player.scheduler.execute(plugin, {
                    player.closeInventory()
                    val gui = PermissionsManager_GroupDetailGUI(plugin, player, groupUUID)
                    player.openInventory(gui.inventory)
                }, null, 0L)
                return
            }

            if (slot == 49) {
                player.scheduler.execute(plugin, {
                    player.closeInventory()
                    val anvilTitle = mm.deserialize("<#8298d9><bold>Add Permission</bold></#8298d9>")
                    plugin.servicesFwk.inventoryGUI.api.openAnvilInput(
                        player,
                        anvilTitle,
                        "Type permission..."
                    ) { perm ->
                        plugin.server.regionScheduler.execute(plugin, player.world, player.location.chunk.x, player.location.chunk.z) {
                            val result = permsAPI.group_addPerm(group.name, perm)
                            if (result == site.ftka.survivalcore.services.permissions.subservices.PermissionsService_GroupsSubservice.PermissionGroup_addPermissionResult.SUCCESS) {
                                plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<green>✔ Added permission '$perm' to group '${group.name}'.</green>"))
                            } else {
                                plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<red>✖ Failed: Permission already exists or error occurred.</red>"))
                            }
                            // Re-open GroupPermsGUI
                            val gui = PermissionsManager_GroupPermsGUI(plugin, player, groupUUID, page)
                            player.openInventory(gui.inventory)
                        }
                    }
                }, null, 0L)
                return
            }

            if (slot == 48 && page > 0) {
                player.scheduler.execute(plugin, {
                    player.closeInventory()
                    val gui = PermissionsManager_GroupPermsGUI(plugin, player, groupUUID, page - 1)
                    player.openInventory(gui.inventory)
                }, null, 0L)
                return
            }

            if (slot == 50 && (page + 1) * 28 < permissionsList.size) {
                player.scheduler.execute(plugin, {
                    player.closeInventory()
                    val gui = PermissionsManager_GroupPermsGUI(plugin, player, groupUUID, page + 1)
                    player.openInventory(gui.inventory)
                }, null, 0L)
                return
            }

            // Clicked a permission item to remove it
            val clickedSlotIndex = contentSlots.indexOf(slot)
            if (clickedSlotIndex != -1) {
                val listIndex = page * 28 + clickedSlotIndex
                if (listIndex < permissionsList.size) {
                    val perm = permissionsList[listIndex]
                    player.scheduler.execute(plugin, {
                        permsAPI.group_removePerm(group.name, perm)
                        plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<green>✔ Removed permission '$perm' from group '${group.name}'.</green>"))
                        setupItems() // Re-render in place
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

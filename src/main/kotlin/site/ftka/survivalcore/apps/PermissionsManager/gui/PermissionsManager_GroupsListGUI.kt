package site.ftka.survivalcore.apps.PermissionsManager.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.inventorygui.interfaces.InventoryGUIOwner
import site.ftka.survivalcore.services.permissions.objects.PermissionGroup

internal class PermissionsManager_GroupsListGUI(
    private val plugin: MClass,
    private val player: Player
) : InventoryGUIOwner {

    override val ownerName = "PermissionsManager_GroupsList_${player.uniqueId}"
    private val inv: Inventory
    private val mm = MiniMessage.miniMessage()
    private val permsAPI = plugin.servicesFwk.permissions.api
    private var groupsList = listOf<PermissionGroup>()

    init {
        val title = mm.deserialize("<#8298d9><bold>Permission Groups</bold></#8298d9>")
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

    private fun getColorWool(colorStr: String): Material {
        return when (colorStr.uppercase()) {
            "BLACK" -> Material.BLACK_WOOL
            "DARK_BLUE", "BLUE" -> Material.BLUE_WOOL
            "DARK_GREEN", "GREEN" -> Material.GREEN_WOOL
            "DARK_AQUA", "AQUA" -> Material.LIGHT_BLUE_WOOL
            "DARK_RED", "RED" -> Material.RED_WOOL
            "DARK_PURPLE", "LIGHT_PURPLE" -> Material.PURPLE_WOOL
            "GOLD", "YELLOW" -> Material.YELLOW_WOOL
            "GRAY" -> Material.LIGHT_GRAY_WOOL
            "DARK_GRAY" -> Material.GRAY_WOOL
            "WHITE" -> Material.WHITE_WOOL
            else -> Material.WHITE_WOOL
        }
    }

    private fun setupItems() {
        inv.clear()

        // Border filling
        val filler = createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray> </gray>")
        for (i in 0 until 54) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, filler)
            }
        }

        // Retrieve and sort groups alphabetically
        groupsList = permsAPI.getGroups().sortedBy { it.name }

        // Place group items in the center grid
        val slots = listOf(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        )

        for (index in groupsList.indices) {
            if (index >= slots.size) break
            val group = groupsList[index]
            val woolMat = getColorWool(group.primaryColor)
            
            val groupItem = createItem(
                woolMat,
                "<${group.primaryColor}><b>${group.displayName}</b></${group.primaryColor}>",
                "<gray>System Name: <white>${group.name}</white></gray>",
                "<gray>Chat Tag: <white>${group.tag}</white></gray>",
                "<gray>Category: <white>${group.category.name}</white></gray>",
                "<gray>Permissions: <white>${group.perms.size}</white></gray>",
                "<gray>Inheritances: <white>${group.inheritances.size}</white></gray>",
                "",
                "<yellow>Click to configure group details</yellow>"
            )
            inv.setItem(slots[index], groupItem)
        }

        // Create Group Button
        val createBtn = createItem(
            Material.EMERALD_BLOCK,
            "<green><bold>+ Create New Group</bold></green>",
            "<gray>Click to create a brand new</gray>",
            "<gray>permission group visually.</gray>"
        )
        inv.setItem(49, createBtn)

        // Close/Exit Button
        val exitBtn = createItem(Material.BARRIER, "<red>Exit</red>")
        inv.setItem(45, exitBtn)
    }

    override fun getInventory(): Inventory = inv

    override fun clickEvent(event: InventoryClickEvent) {
        val clickedInv = event.clickedInventory ?: return
        if (clickedInv == inv) {
            event.isCancelled = true

            val slot = event.slot
            if (slot == 45) {
                player.scheduler.execute(plugin, { player.closeInventory() }, null, 0L)
                return
            }

            if (slot == 49) {
                player.scheduler.execute(plugin, {
                    player.closeInventory()
                    val anvilTitle = mm.deserialize("<#8298d9><bold>Create Group</bold></#8298d9>")
                    plugin.servicesFwk.inventoryGUI.api.openAnvilInput(
                        player,
                        anvilTitle,
                        "Type group name..."
                    ) { name ->
                        plugin.server.regionScheduler.execute(plugin, player.world, player.location.chunk.x, player.location.chunk.z) {
                            val newGroup = permsAPI.createGroup(name)
                            if (newGroup != null) {
                                plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<green>✔ Successfully created group <white>${newGroup.displayName}</white>.</green>"))
                            } else {
                                plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<red>✖ Failed: Group '$name' already exists.</red>"))
                            }
                            // Re-open GroupsListGUI
                            val gui = PermissionsManager_GroupsListGUI(plugin, player)
                            player.openInventory(gui.inventory)
                        }
                    }
                }, null, 0L)
                return
            }

            // Check if they clicked a group wool block
            val slots = listOf(
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
            )

            val clickedIndex = slots.indexOf(slot)
            if (clickedIndex != -1 && clickedIndex < groupsList.size) {
                val group = groupsList[clickedIndex]
                player.scheduler.execute(plugin, {
                    player.closeInventory()
                    val gui = PermissionsManager_GroupDetailGUI(plugin, player, group.uuid)
                    player.openInventory(gui.inventory)
                }, null, 0L)
            }
        }
    }

    override fun dragEvent(event: InventoryDragEvent) {
        if (event.rawSlots.any { it < inv.size }) {
            event.isCancelled = true
        }
    }
}

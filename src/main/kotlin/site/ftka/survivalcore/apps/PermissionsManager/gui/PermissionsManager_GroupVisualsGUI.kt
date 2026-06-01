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

internal class PermissionsManager_GroupVisualsGUI(
    private val plugin: MClass,
    private val player: Player,
    private val groupUUID: UUID
) : InventoryGUIOwner {

    override val ownerName = "PermissionsManager_GroupVisuals_${player.uniqueId}"
    private val inv: Inventory
    private val mm = MiniMessage.miniMessage()
    private val permsAPI = plugin.servicesFwk.permissions.api

    init {
        val group = permsAPI.getGroup(groupUUID)
        val name = group?.displayName ?: "Group"
        val title = mm.deserialize("<#55ffff><bold>$name Visual Config</bold></#55ffff>")
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

        // Set Tag Button
        val tagItem = createItem(
            Material.NAME_TAG,
            "<#ffaa00><b>Set Chat Tag</b></#ffaa00>",
            "<gray>Modify the group's chat prefix tag.</gray>",
            "",
            "<gray>Current Tag: <white>${group.tag}</white></gray>",
            "",
            "<yellow>Click to change prefix tag</yellow>"
        )
        inv.setItem(10, tagItem)

        // Set Primary Color Button
        val primaryColorItem = createItem(
            Material.PINK_DYE,
            "<#ff55ff><b>Set Primary Color</b></#ff55ff>",
            "<gray>Modify the group's main color prefix.</gray>",
            "",
            "<gray>Current: <${group.primaryColor}>${group.primaryColor}</${group.primaryColor}></gray>",
            "",
            "<yellow>Click to change primary color</yellow>"
        )
        inv.setItem(13, primaryColorItem)

        // Set Secondary Color Button
        val secondaryColorItem = createItem(
            Material.PURPLE_DYE,
            "<#55ffff><b>Set Secondary Color</b></#55ffff>",
            "<gray>Modify the group's secondary/highlight color.</gray>",
            "",
            "<gray>Current: <${group.secondaryColor}>${group.secondaryColor}</${group.secondaryColor}></gray>",
            "",
            "<yellow>Click to change secondary color</yellow>"
        )
        inv.setItem(16, secondaryColorItem)

        // Back Button
        val backBtn = createItem(Material.ARROW, "<red>Back to Core Settings</red>")
        inv.setItem(18, backBtn)
    }

    override fun getInventory(): Inventory = inv

    override fun clickEvent(event: InventoryClickEvent) {
        val clickedInv = event.clickedInventory ?: return
        if (clickedInv == inv) {
            event.isCancelled = true

            val group = permsAPI.getGroup(groupUUID) ?: return

            when (event.slot) {
                10 -> {
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        val anvilTitle = mm.deserialize("<#ffaa00><bold>Set Chat Tag</bold></#ffaa00>")
                        plugin.servicesFwk.inventoryGUI.api.openAnvilInput(
                            player,
                            anvilTitle,
                            group.tag
                        ) { newTag ->
                            plugin.server.regionScheduler.execute(plugin, player.world, player.location.chunk.x, player.location.chunk.z) {
                                permsAPI.setTagToGroup(groupUUID, newTag)
                                plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<green>✔ Successfully updated tag for group '${group.name}' to '<white>$newTag</white>'.</green>"))
                                val gui = PermissionsManager_GroupVisualsGUI(plugin, player, groupUUID)
                                player.openInventory(gui.inventory)
                            }
                        }
                    }, null, 0L)
                }
                13 -> {
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        val gui = PermissionsManager_GroupColorPickerGUI(plugin, player, groupUUID, true)
                        player.openInventory(gui.inventory)
                    }, null, 0L)
                }
                16 -> {
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        val gui = PermissionsManager_GroupColorPickerGUI(plugin, player, groupUUID, false)
                        player.openInventory(gui.inventory)
                    }, null, 0L)
                }
                18 -> {
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        val gui = PermissionsManager_GroupDetailGUI(plugin, player, groupUUID)
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

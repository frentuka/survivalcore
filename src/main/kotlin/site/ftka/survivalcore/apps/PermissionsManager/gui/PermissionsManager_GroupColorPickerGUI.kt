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

internal class PermissionsManager_GroupColorPickerGUI(
    private val plugin: MClass,
    private val player: Player,
    private val groupUUID: UUID,
    private val isPrimary: Boolean
) : InventoryGUIOwner {

    override val ownerName = "PermissionsManager_GroupColorPicker_${player.uniqueId}"
    private val inv: Inventory
    private val mm = MiniMessage.miniMessage()
    private val permsAPI = plugin.servicesFwk.permissions.api

    // Hand-picked gorgeous vibrant palette colors mapping to their Material and Hex values
    private val colorPalette = listOf(
        ColorPreset("Pastel Red", "#ff4444", Material.RED_WOOL),
        ColorPreset("Coral Orange", "#ff9900", Material.ORANGE_WOOL),
        ColorPreset("Golden Yellow", "#ffcc00", Material.YELLOW_WOOL),
        ColorPreset("Mint Green", "#55ff55", Material.LIME_WOOL),
        ColorPreset("Cyan Blue", "#00ffff", Material.CYAN_WOOL),
        ColorPreset("Sky Aqua", "#55ffff", Material.LIGHT_BLUE_WOOL),
        ColorPreset("Royal Blue", "#3366ff", Material.BLUE_WOOL),
        ColorPreset("Lilac Purple", "#aa55ff", Material.PURPLE_WOOL),
        ColorPreset("Hot Pink", "#ff55ff", Material.PINK_WOOL),
        ColorPreset("Rose Magenta", "#ff55aa", Material.MAGENTA_WOOL),
        ColorPreset("Soft Gray", "#aaaaaa", Material.LIGHT_GRAY_WOOL),
        ColorPreset("Charcoal Gray", "#555555", Material.GRAY_WOOL),
        ColorPreset("Dark Black", "#111111", Material.BLACK_WOOL),
        ColorPreset("Snow White", "#ffffff", Material.WHITE_WOOL)
    )

    private class ColorPreset(val name: String, val hex: String, val material: Material)

    init {
        val group = permsAPI.getGroup(groupUUID)
        val name = group?.displayName ?: "Group"
        val mode = if (isPrimary) "Primary" else "Secondary"
        val title = mm.deserialize("<#ff55ff><bold>$name $mode Color</bold></#ff55ff>")
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

        // Setup gray border panes
        val filler = createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray> </gray>")
        for (i in 0 until 27) {
            if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, filler)
            }
        }

        // Align the 14 colors symmetrically in columns 1 to 7 across rows 1 and 2
        val targetSlots = listOf(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25
        )

        for (i in colorPalette.indices) {
            val preset = colorPalette[i]
            val slot = targetSlots[i]
            val colorItem = createItem(
                preset.material,
                "<${preset.hex}><b>${preset.name}</b></${preset.hex}>",
                "<gray>Hex Code: <white>${preset.hex}</white></gray>",
                "",
                "<yellow>Click to set as color</yellow>"
            )
            inv.setItem(slot, colorItem)
        }

        // Custom Hex Input Button - placed on the bottom-right corner (slot 26) to mirror slot 18
        val customBtn = createItem(
            Material.NAME_TAG,
            "<gold><b>+ Custom RGB Hex Code</b></gold>",
            "<gray>Input any custom color using a hex</gray>",
            "<gray>format, such as <#ffaa00>#FFAA00</#ffaa00>.</gray>",
            "",
            "<yellow>Click to open anvil input</yellow>"
        )
        inv.setItem(26, customBtn)

        // Back Button - placed on the bottom-left corner (slot 18)
        val backBtn = createItem(Material.ARROW, "<red>Back to Visuals</red>")
        inv.setItem(18, backBtn)
    }

    override fun getInventory(): Inventory = inv

    override fun clickEvent(event: InventoryClickEvent) {
        val clickedInv = event.clickedInventory ?: return
        if (clickedInv == inv) {
            event.isCancelled = true

            val group = permsAPI.getGroup(groupUUID) ?: return
            val slot = event.slot

            if (slot == 18) {
                player.scheduler.execute(plugin, {
                    player.closeInventory()
                    val gui = PermissionsManager_GroupVisualsGUI(plugin, player, groupUUID)
                    player.openInventory(gui.inventory)
                }, null, 0L)
                return
            }

            if (slot == 26) {
                player.scheduler.execute(plugin, {
                    player.closeInventory()
                    val anvilTitle = mm.deserialize("<#ffcc00><b>Type HEX Color</b></#ffcc00>")
                    plugin.servicesFwk.inventoryGUI.api.openAnvilInput(
                        player,
                        anvilTitle,
                        "000000",
                        true // isHexColor = true
                    ) { customHex ->
                        plugin.server.regionScheduler.execute(plugin, player.world, player.location.chunk.x, player.location.chunk.z) {
                            if (isPrimary) {
                                permsAPI.setPrimaryColorToGroup(groupUUID, customHex)
                            } else {
                                permsAPI.setSecondaryColorToGroup(groupUUID, customHex)
                            }
                            plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<green>✔ Successfully updated color to '<white>$customHex</white>'.</green>"))
                            val gui = PermissionsManager_GroupVisualsGUI(plugin, player, groupUUID)
                            player.openInventory(gui.inventory)
                        }
                    }
                }, null, 0L)
                return
            }
 
            // Check if they clicked one of the palette slots
            val targetSlots = listOf(
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25
            )
            val clickedIndex = targetSlots.indexOf(slot)
            if (clickedIndex != -1 && clickedIndex < colorPalette.size) {
                val preset = colorPalette[clickedIndex]
                player.scheduler.execute(plugin, {
                    player.closeInventory()
                    plugin.server.regionScheduler.execute(plugin, player.world, player.location.chunk.x, player.location.chunk.z) {
                        if (isPrimary) {
                            permsAPI.setPrimaryColorToGroup(groupUUID, preset.hex)
                        } else {
                            permsAPI.setSecondaryColorToGroup(groupUUID, preset.hex)
                        }
                        plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<green>✔ Successfully updated color to <${preset.hex}>${preset.name}</${preset.hex}>.</green>"))
                        val gui = PermissionsManager_GroupVisualsGUI(plugin, player, groupUUID)
                        player.openInventory(gui.inventory)
                    }
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

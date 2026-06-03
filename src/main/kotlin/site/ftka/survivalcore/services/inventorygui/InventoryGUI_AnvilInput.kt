package site.ftka.survivalcore.services.inventorygui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import site.ftka.survivalcore.MClass

@Suppress("DEPRECATION")
class InventoryGUI_AnvilInput(
    private val plugin: MClass,
    val player: Player,
    private val title: Component,
    private val placeholderText: String = "Enter text...",
    private val isHexColor: Boolean = false,
    private val callback: (String) -> Unit
) {
    private val originalLevel = player.level
    private val originalExp = player.exp
    private var isCompleted = false
    private var inv: Inventory? = null
    private val mm = MiniMessage.miniMessage()
    private val anvilKey = NamespacedKey(plugin, "virtual_anvil_input")

    init {
        // Temporarily give player levels to satisfy client-side anvil level-cost check
        player.level = 1000

        // Open a real anvil container bound to the player's location so it ticks and calculates on Folia
        val view = player.openAnvil(player.location, true)
        if (view != null) {
            val initialTitle = if (isHexColor) {
                // Set the initial title of the anvil to a comfortable, non-bold "Hex Input"
                val component = mm.deserialize("<gray>Hex Input</gray>")
                LegacyComponentSerializer.legacySection().serialize(component)
            } else {
                PlainTextComponentSerializer.plainText().serialize(title)
            }
            try {
                view.title = initialTitle
            } catch (e: Exception) {
                // Fallback
            }

            val topInv = view.topInventory as? AnvilInventory
            if (topInv != null) {
                this.inv = topInv
                
                // Set the placeholder item in slot 0 (left-most input slot)
                val placeholder = ItemStack(Material.PAPER).apply {
                    itemMeta = itemMeta?.apply {
                        displayName(Component.text(placeholderText))
                        persistentDataContainer.set(anvilKey, PersistentDataType.BYTE, 1.toByte())
                    }
                }
                topInv.setItem(0, placeholder)
            }
        }
    }

    private fun isValidHex(hex: String): Boolean {
        val clean = hex.removePrefix("#").trim()
        return clean.length == 6 && clean.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
    }

    fun handlePrepareAnvil(event: PrepareAnvilEvent) {
        val renameText = event.inventory.renameText ?: ""
        val trimmed = renameText.trim()

        if (isHexColor) {
            val cleanHex = trimmed.removePrefix("#").trim()
            if (isValidHex(cleanHex)) {
                val hexWithHash = "#$cleanHex"
                // Create output paper representing the color preview
                val result = ItemStack(Material.PAPER).apply {
                    itemMeta = itemMeta?.apply {
                        displayName(mm.deserialize("<$hexWithHash><b>Preview $hexWithHash</b></$hexWithHash>"))
                        lore(listOf(mm.deserialize("<gray>Click to confirm this color</gray>")))
                        persistentDataContainer.set(anvilKey, PersistentDataType.BYTE, 1.toByte())
                    }
                }
                event.result = result
            } else {
                // Display the warning block "Invalid #??????" in Slot 2 in real-time
                val result = ItemStack(Material.RED_STAINED_GLASS_PANE).apply {
                    itemMeta = itemMeta?.apply {
                        displayName(mm.deserialize("<red><b>Invalid #??????</b></red>"))
                        lore(listOf(mm.deserialize("<gray>Please enter a valid 6-digit hex code.</gray>")))
                        persistentDataContainer.set(anvilKey, PersistentDataType.BYTE, 1.toByte())
                    }
                }
                event.result = result
            }
        } else {
            // Standard text input logic
            if (trimmed.isNotEmpty() && trimmed != placeholderText) {
                val result = ItemStack(Material.PAPER).apply {
                    itemMeta = itemMeta?.apply {
                        displayName(Component.text(trimmed))
                        persistentDataContainer.set(anvilKey, PersistentDataType.BYTE, 1.toByte())
                    }
                }
                event.result = result
            } else {
                event.result = null
            }
        }
    }

    fun handleClick(event: InventoryClickEvent) {
        // ALWAYS cancel and deny any click inside the active anvil input flow
        event.isCancelled = true
        event.result = org.bukkit.event.Event.Result.DENY

        val clickedInv = event.clickedInventory ?: return
        if (clickedInv == inv) {
            if (event.slot == 2) {
                val resultItem = event.currentItem
                if (resultItem == null || resultItem.type == Material.RED_STAINED_GLASS_PANE) {
                    plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<red>✖ Please enter a valid 6-digit hex code!</red>"))
                    cleanupGhostItems()
                    return
                }

                val text = if (resultItem.hasItemMeta() && resultItem.itemMeta.hasDisplayName()) {
                    resultItem.itemMeta.displayName()?.let {
                        PlainTextComponentSerializer.plainText().serialize(it)
                    } ?: ""
                } else {
                    (inv as? AnvilInventory)?.renameText ?: ""
                }

                val trimmedText = text.trim()
                
                // BULLETPROOF ELIMINATION: Immediately wipe player's predicted cursor item,
                // clear all anvil slots on the server, and force client resync to block leaked paper.
                player.setItemOnCursor(null)
                inv?.setItem(0, null)
                inv?.setItem(1, null)
                inv?.setItem(2, null)
                player.updateInventory()

                if (isHexColor) {
                    val rawText = trimmedText.replace("Preview ", "").trim()
                    val cleanHex = if (rawText.startsWith("#")) rawText.substring(1) else rawText
                    if (isValidHex(cleanHex)) {
                        isCompleted = true
                        player.scheduler.execute(plugin, {
                            player.closeInventory()
                            callback("#$cleanHex")
                        }, null, 0L)
                    }
                } else {
                    if (trimmedText.isNotEmpty() && trimmedText != placeholderText) {
                        isCompleted = true
                        player.scheduler.execute(plugin, {
                            player.closeInventory()
                            callback(trimmedText)
                        }, null, 0L)
                    }
                }
            }
        }
        
        // Trigger delayed cleanup for ANY click during the anvil input flow to erase predicted items
        cleanupGhostItems()
    }

    fun handleDrag(event: InventoryDragEvent) {
        val currentInv = inv ?: return
        if (event.rawSlots.any { it < currentInv.size }) {
            event.isCancelled = true
        }
    }

    fun handleClose(event: InventoryCloseEvent) {
        // Restore player's levels and XP
        player.level = originalLevel
        player.exp = originalExp
        
        // Remove from active anvil inputs map
        plugin.servicesFwk.inventoryGUI.activeAnvilInputs.remove(player.uniqueId)

        // Trigger delayed cleanup to catch any leftover client-predicted ghost items
        cleanupGhostItems()
    }

    /**
     * Eradicates any client-predicted ghost items from the player's cursor and inventory.
     * Checks for the unique custom NamespacedKey tag to strictly target virtual anvil items,
     * ensuring player-owned legitimate survival papers/panes are never touched.
     */
    private fun cleanupGhostItems() {
        player.scheduler.execute(plugin, {
            // 1. Check and wipe cursor if it is a client-predicted ghost item carrying our NBT tag
            val cursor = player.itemOnCursor
            if (cursor.type != Material.AIR) {
                val meta = cursor.itemMeta
                if (meta != null && meta.persistentDataContainer.has(anvilKey, PersistentDataType.BYTE)) {
                    player.setItemOnCursor(null)
                }
            }

            // 2. Scan and remove any leaked ghost papers/panes carrying our NBT tag from player's inventory
            val pInv = player.inventory
            for (i in 0 until pInv.size) {
                val item = pInv.getItem(i)
                if (item != null && item.type != Material.AIR) {
                    val meta = item.itemMeta
                    if (meta != null && meta.persistentDataContainer.has(anvilKey, PersistentDataType.BYTE)) {
                        pInv.setItem(i, null)
                    }
                }
            }

            // 3. Force inventory packet resync
            player.updateInventory()
        }, null, 1L)
    }
}

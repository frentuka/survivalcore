package site.ftka.survivalcore.apps.PlayerDataManager.gui

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
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.UUID

internal class PlayerDataManager_MainGUI(
    private val plugin: MClass,
    private val player: Player,
    private val targetUUID: UUID
) : InventoryGUIOwner {

    override val ownerName = "PlayerDataManager_Main_${player.uniqueId}"
    private val inv: Inventory
    private val mm = MiniMessage.miniMessage()
    private var loadedData: PlayerData? = null

    init {
        val targetName = plugin.essentialsFwk.usernameTracker.getName(targetUUID) ?: "Unknown"
        val title = mm.deserialize("<#ffcc00><bold>$targetName's Data</bold></#ffcc00>")
        inv = plugin.servicesFwk.inventoryGUI.api.createInventory(this, 27, title)
        
        // Show loading state
        inv.setItem(13, ItemStack(Material.CLOCK).apply {
            itemMeta = itemMeta?.apply { displayName(mm.deserialize("<yellow>Loading player data...</yellow>")) }
        })

        loadData()
    }

    private fun loadData() {
        plugin.servicesFwk.playerData.inout_ss.get(targetUUID, async = true).thenAccept { pdata ->
            player.scheduler.execute(plugin, {
                if (pdata != null) {
                    loadedData = pdata
                    setupItems(pdata)
                } else {
                    inv.setItem(13, ItemStack(Material.BARRIER).apply {
                        itemMeta = itemMeta?.apply { displayName(mm.deserialize("<red>Failed to load PlayerData</red>")) }
                    })
                }
            }, null, 0L)
        }
    }

    private fun createItem(material: Material, name: String, vararg lore: String): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item
        meta.displayName(mm.deserialize(name))
        meta.lore(lore.map { mm.deserialize(it) })
        item.itemMeta = meta
        return item
    }

    private fun setupItems(pdata: PlayerData) {
        inv.clear()

        val filler = createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray> </gray>")
        for (i in 0 until 27) {
            if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, filler)
            }
        }

        // Info Orb
        val infoItem = createItem(
            Material.BEACON,
            "<#00ffcc><bold>Profile Information</bold></#00ffcc>",
            "<gray>UUID: <white>${pdata.uuid}</white></gray>",
            "<gray>First Join: <white>${pdata.information?.firstConnection}</white></gray>",
            "<gray>Gamemode: <white>${pdata.state?.gameMode}</white></gray>",
            "<gray>Health: <white>${pdata.state?.health}</white></gray>",
            "<gray>Unlocked Chunks: <white>${pdata.unlockedChunks.size}</white></gray>"
        )
        inv.setItem(11, infoItem)

        // Edit Data Button
        val editBtn = createItem(
            Material.WRITABLE_BOOK,
            "<#ff9900><bold>Edit Data</bold></#ff9900>",
            "<gray>Open the advanced JSON editor</gray>",
            "<gray>to modify any field.</gray>",
            "",
            "<yellow>Click to open Editor</yellow>"
        )
        inv.setItem(13, editBtn)

        // Delete Data Button
        val deleteBtn = createItem(
            Material.TNT,
            "<#ff3333><bold>WIPE PLAYER DATA</bold></#ff3333>",
            "<gray>Completely delete this player</gray>",
            "<gray>from the server database.</gray>",
            "",
            "<red>⚠️ DANGER ZONE ⚠️</red>"
        )
        inv.setItem(15, deleteBtn)

        // Back Button
        val backBtn = createItem(Material.ARROW, "<red>Back</red>")
        inv.setItem(18, backBtn)
    }

    override fun getInventory(): Inventory = inv

    override fun clickEvent(event: InventoryClickEvent) {
        val clickedInv = event.clickedInventory ?: return
        if (clickedInv == inv) {
            event.isCancelled = true

            when (event.slot) {
                13 -> {
                    if (loadedData != null) {
                        player.scheduler.execute(plugin, {
                            player.closeInventory()
                            val gui = PlayerDataManager_EditorGUI(plugin, player, targetUUID)
                            player.openInventory(gui.inventory)
                        }, null, 0L)
                    }
                }
                15 -> {
                    if (loadedData != null) {
                        player.scheduler.execute(plugin, {
                            player.closeInventory()
                            val gui = PlayerDataManager_DeleteConfirmGUI(plugin, player, targetUUID)
                            player.openInventory(gui.inventory)
                        }, null, 0L)
                    }
                }
                18 -> {
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        val gui = PlayerDataManager_OnlineListGUI(plugin, player)
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

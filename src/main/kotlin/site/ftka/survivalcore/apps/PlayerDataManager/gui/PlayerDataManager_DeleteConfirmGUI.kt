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
import java.util.UUID
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.future.await

internal class PlayerDataManager_DeleteConfirmGUI(
    private val plugin: MClass,
    private val player: Player,
    private val targetUUID: UUID
) : InventoryGUIOwner {

    override val ownerName = "PlayerDataManager_Delete_${player.uniqueId}"
    private val inv: Inventory
    private val mm = MiniMessage.miniMessage()

    init {
        val title = mm.deserialize("<dark_red><bold>ARE YOU SURE?</bold></dark_red>")
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
        val cancelBtn = createItem(
            Material.GREEN_WOOL,
            "<green><bold>CANCEL</bold></green>",
            "<gray>Do not delete.</gray>",
            "<gray>Return to dashboard.</gray>"
        )

        val confirmBtn = createItem(
            Material.RED_WOOL,
            "<red><bold>CONFIRM DELETION</bold></red>",
            "<gray>This action is irreversible.</gray>",
            "<gray>Target UUID: $targetUUID</gray>"
        )

        // Fill left side with cancel, right side with confirm
        for (i in 0 until 27) {
            if (i % 9 < 4) inv.setItem(i, cancelBtn)
            else if (i % 9 > 4) inv.setItem(i, confirmBtn)
        }
    }

    override fun getInventory(): Inventory = inv

    override fun clickEvent(event: InventoryClickEvent) {
        val clickedInv = event.clickedInventory ?: return
        if (clickedInv == inv) {
            event.isCancelled = true

            val slot = event.slot
            if (slot % 9 < 4) {
                // Cancel
                player.scheduler.execute(plugin, {
                    player.closeInventory()
                    val gui = PlayerDataManager_MainGUI(plugin, player, targetUUID)
                    player.openInventory(gui.inventory)
                }, null, 0L)
            } else if (slot % 9 > 4) {
                // Confirm
                if (!plugin.servicesFwk.permissions.api.player_hasPerm_locally(player.uniqueId, "staff.admin.playerdatamanager")) {
                    player.sendMessage(mm.deserialize("<red>You lack the required permission to confirm this deletion.</red>"))
                    player.closeInventory()
                    return
                }

                player.scheduler.execute(plugin, {
                    player.closeInventory()
                    executeWipe()
                }, null, 0L)
            }
        }
    }

    private fun executeWipe() {
        player.sendMessage(mm.deserialize("<red>Initiating player data wipe for $targetUUID...</red>"))

        // 0. Prevent World Corruption by destroying BorderRegion first
        GlobalScope.launch {
            val pdata = plugin.servicesFwk.playerData.inout_ss.get(targetUUID, async = true).await()
            val region = pdata?.borderRegion

            if (region != null && region.blocks.isNotEmpty()) {
                player.sendMessage(mm.deserialize("<yellow>Restoring chunk borders to prevent world corruption...</yellow>"))
                val world = plugin.server.worlds.first()
                val future = plugin.servicesFwk.chunkBorder.api.destroyRegion(world, region)
                try {
                    future.join()
                    player.sendMessage(mm.deserialize("<green>Successfully restored border blocks.</green>"))
                } catch (e: Exception) {
                    player.sendMessage(mm.deserialize("<dark_red>Failed to restore some border blocks!</dark_red>"))
                    e.printStackTrace()
                }
            }

            // Execute the rest of the wipe
            executeWipePostBorder()
        }
    }

    private fun executeWipePostBorder() {
        // 1. Kick if online
        val targetPlayer = plugin.server.getPlayer(targetUUID)
        targetPlayer?.kick(mm.deserialize("<red>Your data has been wiped by an administrator.</red>"))

        // 2. Remove from Memory
        plugin.servicesFwk.playerData.data.removePlayerData(targetUUID)
        plugin.servicesFwk.playerData.caching_ss.deleteCachedData(targetUUID)

        // 3. Remove from Redis
        plugin.essentialsFwk.database.api.del(targetUUID.toString(), true).thenAccept { success ->
            player.scheduler.execute(plugin, {
                if (success) {
                    player.sendMessage(mm.deserialize("<green>Successfully deleted Redis database entry.</green>"))
                } else {
                    player.sendMessage(mm.deserialize("<yellow>Redis entry did not exist or failed to delete.</yellow>"))
                }
            }, null, 0L)
        }

        // 4. Delete Emergency Dumps
        plugin.servicesFwk.playerData.emergency_ss.deleteEmergencyDump(targetUUID)
        player.sendMessage(mm.deserialize("<green>Cleared any local emergency dumps.</green>"))

        // 5. Unclaim Territory
        val claims = plugin.servicesFwk.territory.claims.entries
        val chunksToRemove = mutableListOf<Pair<Int, Int>>()
        for ((chunkKey, ownerUUID) in claims) {
            if (ownerUUID == targetUUID) {
                chunksToRemove.add(chunkKey)
            }
        }
        
        var claimCount = 0
        for (chunk in chunksToRemove) {
            plugin.servicesFwk.territory.unclaimChunk(chunk.first, chunk.second)
            claimCount++
        }
        
        if (claimCount > 0) {
            player.sendMessage(mm.deserialize("<green>Unclaimed $claimCount chunks owned by the player.</green>"))
        }

        player.sendMessage(mm.deserialize("<gold><bold>Wipe complete.</bold></gold>"))
    }

    override fun dragEvent(event: InventoryDragEvent) {
        if (event.rawSlots.any { it < inv.size }) {
            event.isCancelled = true
        }
    }
}

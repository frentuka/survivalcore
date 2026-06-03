package site.ftka.survivalcore.apps.PlayerDataManager.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.chat.objects.ChatScreen
import site.ftka.survivalcore.essentials.chat.objects.ChatScreenPage
import site.ftka.survivalcore.services.inventorygui.interfaces.InventoryGUIOwner

internal class PlayerDataManager_OnlineListGUI(
    private val plugin: MClass,
    private val player: Player
) : InventoryGUIOwner {

    override val ownerName = "PlayerDataManager_Online_${player.uniqueId}"
    private val inv: Inventory
    private val mm = MiniMessage.miniMessage()

    // Store mapped UUIDs to slots so we can fetch them on click
    private val slotUUIDMap = mutableMapOf<Int, java.util.UUID>()

    init {
        val title = mm.deserialize("<#ffcc00><bold>Online Players</bold></#ffcc00>")
        inv = plugin.servicesFwk.inventoryGUI.api.createInventory(this, 54, title)
        setupItems()
    }

    private fun setupItems() {
        val filler = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta?.apply { displayName(Component.empty()) }
        }
        for (i in 0 until 54) {
            if (i >= 45) {
                inv.setItem(i, filler)
            }
        }

        // Add online players
        val activeProfiles = plugin.servicesFwk.playerData.data.getPlayerDataMap()
        var slot = 0
        for ((uuid, pdata) in activeProfiles) {
            if (slot >= 45) break
            
            val head = ItemStack(Material.PLAYER_HEAD)
            val meta = head.itemMeta as SkullMeta
            val name = plugin.essentialsFwk.usernameTracker.getName(uuid) ?: "Unknown"
            
            val bukkitPlayer = plugin.server.getPlayer(uuid)
            if (bukkitPlayer != null) {
                meta.owningPlayer = bukkitPlayer
            }
            
            meta.displayName(mm.deserialize("<green>$name</green>"))
            meta.lore(listOf(
                mm.deserialize("<gray>UUID: $uuid</gray>"),
                mm.deserialize("<yellow>Click to manage player data!</yellow>")
            ))
            head.itemMeta = meta
            
            inv.setItem(slot, head)
            slotUUIDMap[slot] = uuid
            slot++
        }

        // Add search button
        val searchBtn = ItemStack(Material.OAK_SIGN).apply {
            val meta = itemMeta
            meta?.displayName(mm.deserialize("<#66ccff><bold>Search Offline Player</bold></#66ccff>"))
            meta?.lore(listOf(
                mm.deserialize("<gray>Click to enter a username</gray>"),
                mm.deserialize("<gray>via chat text input.</gray>")
            ))
            itemMeta = meta
        }
        inv.setItem(49, searchBtn)
    }

    override fun getInventory(): Inventory = inv

    override fun clickEvent(event: InventoryClickEvent) {
        val clickedInv = event.clickedInventory ?: return
        if (clickedInv == inv) {
            event.isCancelled = true

            if (slotUUIDMap.containsKey(event.slot)) {
                val targetUUID = slotUUIDMap[event.slot]!!
                player.scheduler.execute(plugin, {
                    player.closeInventory()
                    val nextGui = PlayerDataManager_MainGUI(plugin, player, targetUUID)
                    player.openInventory(nextGui.inventory)
                }, null, 0L)
                return
            }

            if (event.slot == 49) {
                // Search Button logic
                player.scheduler.execute(plugin, {
                    player.closeInventory()
                    startSearchChatScreen()
                }, null, 0L)
            }
        }
    }

    private fun startSearchChatScreen() {
        val searchScreen = object : ChatScreen() {
            override val name = "PlayerDataManager_Search"
            override var screenContent = mutableMapOf(
                "home" to ChatScreenPage(
                    message = "\n<#66ccff><bold>Player Search</bold></#66ccff>\n<gray>Please type the username of the player you want to manage.</gray>\n<gray>Type</gray> <red>/exitscreen</red> <gray>to cancel.</gray>\n",
                    process = { mm.deserialize(it) },
                    onChat = { input, sender ->
                        val targetUUID = plugin.essentialsFwk.usernameTracker.getUUID(input)
                        plugin.essentialsFwk.chat.api.stopScreen(sender.uniqueId, name)
                        
                        sender.scheduler.execute(plugin, {
                            if (targetUUID != null) {
                                val nextGui = PlayerDataManager_MainGUI(plugin, sender, targetUUID)
                                sender.openInventory(nextGui.inventory)
                            } else {
                                sender.sendMessage(mm.deserialize("<red>Player '$input' not found in tracking database.</red>"))
                            }
                        }, null, 0L)
                    }
                )
            )
        }
        
        plugin.essentialsFwk.chat.api.showScreen(player.uniqueId, searchScreen)
    }

    override fun dragEvent(event: InventoryDragEvent) {
        if (event.rawSlots.any { it < inv.size }) {
            event.isCancelled = true
        }
    }
}

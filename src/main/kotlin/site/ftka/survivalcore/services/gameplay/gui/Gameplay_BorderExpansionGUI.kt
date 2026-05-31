package site.ftka.survivalcore.services.gameplay.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.gameplay.objects.ChunkPriceOption
import site.ftka.survivalcore.services.gameplay.objects.PriceCalculator
import site.ftka.survivalcore.services.inventorygui.interfaces.InventoryGUIOwner

class Gameplay_BorderExpansionGUI(
    private val plugin: MClass,
    private val player: Player,
    private val chunkX: Int,
    private val chunkZ: Int,
    private val unlockedChunks: List<Pair<Int, Int>>
) : InventoryGUIOwner {

    override val ownerName = "BorderExpansion_${player.uniqueId}"
    private val inv: Inventory
    private val options: List<ChunkPriceOption> = PriceCalculator.calculateChunkPrice(player.world, unlockedChunks, chunkX, chunkZ)
    
    // Map of slot ID to the corresponding price option
    private val optionSlots = mutableMapOf<Int, ChunkPriceOption>()

    init {
        val title = Component.text("Expand Territory").color(NamedTextColor.DARK_AQUA)
        inv = plugin.servicesFwk.inventoryGUI.api.createInventory(this, 27, title)
        setupItems()
    }

    private fun setupItems() {
        val slots = when (options.size) {
            1 -> listOf(13)
            2 -> listOf(11, 15)
            3 -> listOf(11, 13, 15)
            else -> (9..(9 + options.size - 1)).toList()
        }

        for (i in options.indices) {
            val option = options[i]
            val slot = slots.getOrNull(i) ?: continue
            
            val iconMat = if (option.type == site.ftka.survivalcore.services.gameplay.objects.PaymentOptionType.BIOME_FAST_TRACK) {
                option.items.firstOrNull()?.type ?: Material.EMERALD_BLOCK
            } else {
                Material.EMERALD_BLOCK
            }

            val icon = ItemStack(iconMat).apply {
                itemMeta = itemMeta?.apply {
                    displayName(Component.text(option.type.title).color(NamedTextColor.GREEN))
                    val loreList = mutableListOf<Component>()
                    
                    if (option.discountDescription != null) {
                        loreList.add(Component.text(option.discountDescription).color(NamedTextColor.GOLD))
                        loreList.add(Component.empty())
                    }
                    
                    loreList.add(Component.text("Requires:").color(NamedTextColor.WHITE))
                    for (cost in option.items) {
                        loreList.add(
                            Component.text("- ${cost.amount}x ").color(NamedTextColor.GRAY)
                                .append(Component.translatable(cost.type.translationKey()).color(NamedTextColor.WHITE))
                        )
                    }
                    loreList.add(Component.empty())
                    loreList.add(Component.text("Click to purchase!").color(NamedTextColor.YELLOW))
                    lore(loreList)
                }
            }
            inv.setItem(slot, icon)
            optionSlots[slot] = option
        }
        
        // Add Admin Bypass / Test Option Button
        if (player.hasPermission("survivalcore.admin") || player.isOp) {
            val firstChunk = unlockedChunks.firstOrNull()
            val originX = firstChunk?.first ?: chunkX
            val originZ = firstChunk?.second ?: chunkZ
            val dx = chunkX - originX
            val dz = chunkZ - originZ
            val distanceToSpawn = kotlin.math.sqrt((dx * dx + dz * dz).toDouble())
            val count = unlockedChunks.size
            
            var adjacentOwned = 0
            val neighbors = listOf(Pair(chunkX + 1, chunkZ), Pair(chunkX - 1, chunkZ), Pair(chunkX, chunkZ + 1), Pair(chunkX, chunkZ - 1))
            for (n in neighbors) if (unlockedChunks.contains(n)) adjacentOwned++

            val testIcon = ItemStack(Material.COMMAND_BLOCK).apply {
                itemMeta = itemMeta?.apply {
                    displayName(Component.text("Admin Bypass & Debug Info").color(NamedTextColor.LIGHT_PURPLE))
                    lore(listOf(
                        Component.text("Price Estimation Data:").color(NamedTextColor.GOLD),
                        Component.text("- Distance to Spawn: ").color(NamedTextColor.GRAY).append(Component.text(String.format("%.1f chunks", distanceToSpawn)).color(NamedTextColor.WHITE)),
                        Component.text("- Owned Chunks Base: ").color(NamedTextColor.GRAY).append(Component.text("$count chunks").color(NamedTextColor.WHITE)),
                        Component.text("- Adjacent Neighbors: ").color(NamedTextColor.GRAY).append(Component.text("$adjacentOwned / 4").color(NamedTextColor.WHITE)),
                        Component.empty(),
                        Component.text("Click to claim for free!").color(NamedTextColor.YELLOW)
                    ))
                }
            }
            inv.setItem(26, testIcon)
        }
    }

    override fun getInventory(): Inventory = inv

    override fun clickEvent(event: InventoryClickEvent) {
        val clickedInv = event.clickedInventory ?: return
        
        if (clickedInv == inv) {
            event.isCancelled = true
            
            if (event.slot == 26 && event.currentItem?.type == Material.COMMAND_BLOCK) {
                handleForcePurchase()
                return
            }
            
            val option = optionSlots[event.slot]
            if (option != null) {
                handlePurchase(option)
            }
        } else {
            if (event.isShiftClick) {
                event.isCancelled = true
            }
        }
    }

    override fun dragEvent(event: InventoryDragEvent) {
        if (event.rawSlots.any { it < inv.size }) {
            event.isCancelled = true
        }
    }

    private fun handlePurchase(option: ChunkPriceOption) {
        // Check if player has all required items for this option
        var hasAllItems = true
        for (cost in option.items) {
            if (!player.inventory.containsAtLeast(ItemStack(cost.type), cost.amount)) {
                hasAllItems = false
                break
            }
        }

        if (hasAllItems) {
            // Deduct the costs
            for (cost in option.items) {
                player.inventory.removeItem(ItemStack(cost.type, cost.amount))
            }
            
            val success = plugin.servicesFwk.territory.claimChunk(player.uniqueId, chunkX, chunkZ)
            if (success) {
                player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
                player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                player.sendActionBar(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<bold><gold>⭐ Territory Expanded! ⭐</gold></bold>"))
                player.world.spawnParticle(org.bukkit.Particle.TOTEM_OF_UNDYING, player.location.add(0.0, 1.0, 0.0), 50, 1.0, 1.0, 1.0, 0.5)
                player.world.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, player.location.add(0.0, 1.0, 0.0), 30, 1.0, 1.0, 1.0, 0.1)
            } else {
                // Refund if failed (e.g., already claimed)
                for (cost in option.items) {
                    player.inventory.addItem(ItemStack(cost.type, cost.amount))
                }
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
                player.sendActionBar(Component.text("Failed to claim region. Is it already claimed?").color(NamedTextColor.RED))
            }
        } else {
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
            player.sendActionBar(Component.text("You do not have enough resources for this option!").color(NamedTextColor.RED))
        }
        player.closeInventory()
    }

    private fun handleForcePurchase() {
        val success = plugin.servicesFwk.territory.claimChunk(player.uniqueId, chunkX, chunkZ)
        if (success) {
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
            player.sendActionBar(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<bold><gold>⭐ Territory Force-Claimed! ⭐</gold></bold>"))
            player.world.spawnParticle(org.bukkit.Particle.TOTEM_OF_UNDYING, player.location.add(0.0, 1.0, 0.0), 50, 1.0, 1.0, 1.0, 0.5)
            player.world.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, player.location.add(0.0, 1.0, 0.0), 30, 1.0, 1.0, 1.0, 0.1)
        } else {
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
            player.sendActionBar(Component.text("Failed to claim region. Is it already claimed?").color(NamedTextColor.RED))
        }
        player.closeInventory()
    }
}

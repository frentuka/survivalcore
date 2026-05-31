package site.ftka.survivalcore.services.gameplay.objects

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.inventory.ItemStack
import kotlin.math.sqrt

enum class PaymentOptionType(val title: String) {
    UNIVERSAL("Universal Cost"),
    BIOME_FAST_TRACK("Biome Fast-Track")
}

data class ChunkPriceOption(
    val type: PaymentOptionType,
    val items: List<ItemStack>,
    val discountDescription: String? = null
)

object PriceCalculator {

    fun calculateChunkPrice(
        world: World,
        unlockedChunks: List<Pair<Int, Int>>,
        targetChunkX: Int,
        targetChunkZ: Int
    ): List<ChunkPriceOption> {
        val count = unlockedChunks.size
        
        // 1. Geometric Contiguity Multiplier
        // Check how many of the target's 4 neighbors are owned by the player
        var adjacentOwned = 0
        val neighbors = listOf(
            Pair(targetChunkX + 1, targetChunkZ),
            Pair(targetChunkX - 1, targetChunkZ),
            Pair(targetChunkX, targetChunkZ + 1),
            Pair(targetChunkX, targetChunkZ - 1)
        )
        for (n in neighbors) {
            if (unlockedChunks.contains(n)) {
                adjacentOwned++
            }
        }

        var contiguityMultiplier = 1.0
        var contiguityDesc: String? = null

        if (adjacentOwned >= 3) {
            contiguityMultiplier = 0.5 // Infill discount
            contiguityDesc = "Infill Discount (-50%)"
        } else if (count > 0) {
            // Calculate true snaking index (Isoperimetric Quotient variant)
            val chunkSet = unlockedChunks.toSet()
            var currentPerimeter = 0
            for (chunk in chunkSet) {
                if (!chunkSet.contains(Pair(chunk.first + 1, chunk.second))) currentPerimeter++
                if (!chunkSet.contains(Pair(chunk.first - 1, chunk.second))) currentPerimeter++
                if (!chunkSet.contains(Pair(chunk.first, chunk.second + 1))) currentPerimeter++
                if (!chunkSet.contains(Pair(chunk.first, chunk.second - 1))) currentPerimeter++
            }
            val newArea = count + 1
            val newPerimeter = currentPerimeter + 4 - (2 * adjacentOwned)
            val snakingIndex = newPerimeter.toDouble() / kotlin.math.sqrt(newArea.toDouble())
            
            // 4.0 is a perfect square. Allow up to 4.5 before penalizing.
            if (snakingIndex > 4.5) {
                val snakingTax = (snakingIndex - 4.5) * 0.15
                contiguityMultiplier = 1.0 + snakingTax
                val taxPct = (snakingTax * 100).toInt()
                contiguityDesc = "Snaking Tax (+$taxPct%)"
            }
        }

        // 2. Base Universal Items
        val (item1, item2, item3) = when {
            count < 8 -> Triple(Material.COBBLESTONE, Material.OAK_LOG, Material.WHEAT)
            count < 20 -> Triple(Material.IRON_INGOT, Material.COAL, Material.COPPER_INGOT)
            count < 50 -> Triple(Material.GOLD_INGOT, Material.REDSTONE, Material.LAPIS_LAZULI)
            else -> Triple(Material.DIAMOND, Material.EMERALD, Material.ENDER_PEARL)
        }

        // 3. Distance from origin chunk overhead
        // The origin is the player's very first unlocked chunk. If they have none, it's the target chunk itself (distance = 0)
        val firstChunk = unlockedChunks.firstOrNull()
        val originX = firstChunk?.first ?: targetChunkX
        val originZ = firstChunk?.second ?: targetChunkZ
        val dx = targetChunkX - originX
        val dz = targetChunkZ - originZ
        val distanceToSpawn = sqrt((dx * dx + dz * dz).toDouble())
        val spawnDistanceOverhead = (distanceToSpawn * 0.4).toInt()

        var amt1 = 1 + (count * 0.4).toInt() + spawnDistanceOverhead
        var amt2 = 1 + (count * 0.5).toInt() + (spawnDistanceOverhead * 1.5).toInt()
        var amt3 = if (count > 3 || distanceToSpawn > 5.0) {
            1 + (count * 0.2).toInt() + (spawnDistanceOverhead * 0.8).toInt()
        } else {
            0
        }

        // Apply Contiguity Multiplier
        amt1 = (amt1 * contiguityMultiplier).toInt().coerceAtLeast(1)
        amt2 = (amt2 * contiguityMultiplier).toInt().coerceAtLeast(1)
        if (amt3 > 0) amt3 = (amt3 * contiguityMultiplier).toInt().coerceAtLeast(1)

        val universalItems = mutableListOf<ItemStack>()
        universalItems.add(ItemStack(item1, amt1.coerceIn(1, 64)))
        universalItems.add(ItemStack(item2, amt2.coerceIn(1, 64)))
        if (amt3 > 0) {
            universalItems.add(ItemStack(item3, amt3.coerceIn(1, 64)))
        }
        val options = mutableListOf<ChunkPriceOption>()
        options.add(ChunkPriceOption(PaymentOptionType.UNIVERSAL, universalItems, contiguityDesc))

        // 4. Biome Fast-Track Items
        val biome = world.getBiome(targetChunkX * 16 + 8, 64, targetChunkZ * 16 + 8)
        val biomeItems = getBiomeSpecificMaterials(biome)
        if (biomeItems != null) {
            var bAmt1 = (amt1 * 0.4).toInt().coerceAtLeast(1)
            var bAmt2 = (amt2 * 0.4).toInt().coerceAtLeast(1)
            var bAmt3 = if (amt3 > 0) (amt3 * 0.4).toInt().coerceAtLeast(1) else 0
            
            // Rotating Market Discount Check
            val currentDiscountBiomeCategory = getRotatingMarketDiscountCategory()
            var biomeDesc = contiguityDesc
            if (isBiomeInCategory(biome, currentDiscountBiomeCategory)) {
                bAmt1 = (bAmt1 * 0.5).toInt().coerceAtLeast(1)
                bAmt2 = (bAmt2 * 0.5).toInt().coerceAtLeast(1)
                if (bAmt3 > 0) bAmt3 = (bAmt3 * 0.5).toInt().coerceAtLeast(1)
                val marketString = "Market Discount (-50%)"
                biomeDesc = if (biomeDesc != null) "$biomeDesc, $marketString" else marketString
            }

            val fastTrackItems = mutableListOf<ItemStack>()
            if (biomeItems.size >= 1) fastTrackItems.add(ItemStack(biomeItems[0], bAmt1.coerceIn(1, 64)))
            if (biomeItems.size >= 2) fastTrackItems.add(ItemStack(biomeItems[1], bAmt2.coerceIn(1, 64)))
            if (bAmt3 > 0 && biomeItems.size >= 3) fastTrackItems.add(ItemStack(biomeItems[2], bAmt3.coerceIn(1, 64)))
            
            if (fastTrackItems.isNotEmpty()) {
                options.add(ChunkPriceOption(PaymentOptionType.BIOME_FAST_TRACK, fastTrackItems, biomeDesc))
            }
        }

        return options
    }

    @Suppress("DEPRECATION")
    private fun getBiomeSpecificMaterials(biome: Biome): List<Material>? {
        val bName = biome.name()
        return when {
            bName.contains("DESERT") -> listOf(Material.SAND, Material.CACTUS, Material.TERRACOTTA)
            bName.contains("BADLANDS") -> listOf(Material.RED_SAND, Material.TERRACOTTA, Material.GOLD_NUGGET)
            bName.contains("OCEAN") || bName.contains("BEACH") -> listOf(Material.PRISMARINE_SHARD, Material.KELP, Material.COD)
            bName.contains("ICE") || bName.contains("SNOW") || bName.contains("FROZEN") -> listOf(Material.SNOWBALL, Material.PACKED_ICE, Material.SPRUCE_LOG)
            bName.contains("JUNGLE") -> listOf(Material.JUNGLE_LOG, Material.COCOA_BEANS, Material.BAMBOO)
            bName.contains("SWAMP") || bName.contains("MANGROVE") -> listOf(Material.SLIME_BALL, Material.CLAY_BALL, Material.LILY_PAD)
            bName.contains("FOREST") || bName.contains("TAIGA") || bName.contains("GROVE") -> listOf(Material.SPRUCE_LOG, Material.SWEET_BERRIES, Material.MOSS_BLOCK)
            bName.contains("PLAINS") || bName.contains("MEADOW") -> listOf(Material.WHEAT, Material.PUMPKIN, Material.MELON)
            bName.contains("MOUNTAIN") || bName.contains("PEAKS") -> listOf(Material.EMERALD, Material.COAL, Material.RAW_IRON)
            bName.contains("NETHER") || bName.contains("CRIMSON") || bName.contains("WARPED") -> listOf(Material.QUARTZ, Material.NETHERRACK, Material.CRIMSON_FUNGUS)
            bName.contains("END") -> listOf(Material.END_STONE, Material.CHORUS_FRUIT, Material.ENDER_PEARL)
            else -> listOf(Material.DIRT, Material.GRAVEL, Material.FLINT) // Fallback fast track
        }
    }

    enum class MarketCategory {
        AQUATIC,
        ARID,
        COLD,
        FOREST
    }

    private fun getRotatingMarketDiscountCategory(): MarketCategory {
        // Rotates every 8 hours
        val timeSlots = System.currentTimeMillis() / (1000 * 60 * 60 * 8)
        val categories = listOf(MarketCategory.AQUATIC, MarketCategory.ARID, MarketCategory.COLD, MarketCategory.FOREST)
        return categories[(timeSlots % categories.size).toInt()]
    }

    @Suppress("DEPRECATION")
    private fun isBiomeInCategory(biome: Biome, category: MarketCategory): Boolean {
        val bName = biome.name()
        return when (category) {
            MarketCategory.AQUATIC -> bName.contains("OCEAN") || bName.contains("SWAMP") || bName.contains("RIVER") || bName.contains("BEACH") || bName.contains("MANGROVE")
            MarketCategory.ARID -> bName.contains("DESERT") || bName.contains("BADLANDS") || bName.contains("SAVANNA")
            MarketCategory.COLD -> bName.contains("ICE") || bName.contains("SNOW") || bName.contains("FROZEN") || bName.contains("TAIGA") || bName.contains("GROVE")
            MarketCategory.FOREST -> bName.contains("FOREST") || bName.contains("JUNGLE") || bName.contains("PLAINS") || bName.contains("MEADOW")
        }
    }
}

package site.ftka.survivalcore.services.chunkborder.subservices

import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.block.Container
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chunkborder.ChunkBorderService
import site.ftka.survivalcore.services.chunkborder.objects.BorderRegion
import site.ftka.survivalcore.services.chunkborder.objects.CachedBlock
import java.util.Base64
import java.io.ByteArrayInputStream
import org.bukkit.util.io.BukkitObjectInputStream
import java.util.concurrent.CompletableFuture

class Border_RestorationSubservice(private val service: ChunkBorderService, private val plugin: MClass) {

    fun restoreRegion(world: org.bukkit.World, region: BorderRegion): CompletableFuture<Void> {
        val blocks = region.blocks

        // Group blocks by target chunk
        val chunkCoordGroups = blocks.entries.groupBy { 
            val x = BorderRegion.unpackX(it.key)
            val z = BorderRegion.unpackZ(it.key)
            Pair(x shr 4, z shr 4)
        }

        val futures = mutableListOf<CompletableFuture<Void>>()

        for ((chunkOffset, entries) in chunkCoordGroups) {
            val future = CompletableFuture<Void>()
            futures.add(future)
            
            // Sort blocks: solids first, then non-solids
            val solidBlocks = mutableListOf<Map.Entry<Long, CachedBlock>>()
            val nonSolidBlocks = mutableListOf<Map.Entry<Long, CachedBlock>>()

            for (entry in entries) {
                if (entry.value.isSolid) {
                    solidBlocks.add(entry)
                } else {
                    nonSolidBlocks.add(entry)
                }
            }

            // Schedule restoration on the target chunk's region thread
            plugin.server.regionScheduler.run(plugin, world, chunkOffset.first, chunkOffset.second) {
                for (entry in solidBlocks) {
                    restoreBlock(world, entry.key, entry.value)
                }
                for (entry in nonSolidBlocks) {
                    restoreBlock(world, entry.key, entry.value)
                }
                future.complete(null)
            }
        }
        
        return CompletableFuture.allOf(*futures.toTypedArray()).thenRun {
            region.blocks.clear()
        }
    }

    fun restoreSharedEdges(world: org.bukkit.World, region: BorderRegion, newChunkX: Int, newChunkZ: Int): CompletableFuture<Void> {
        val blocksToRestore = region.blocks.filter { entry ->
            val x = BorderRegion.unpackX(entry.key)
            val z = BorderRegion.unpackZ(entry.key)
            (x shr 4) == newChunkX && (z shr 4) == newChunkZ
        }

        if (blocksToRestore.isEmpty()) return CompletableFuture.completedFuture(null)

        val future = CompletableFuture<Void>()
        val solidBlocks = mutableListOf<Map.Entry<Long, CachedBlock>>()
        val nonSolidBlocks = mutableListOf<Map.Entry<Long, CachedBlock>>()

        for (entry in blocksToRestore) {
            if (entry.value.isSolid) solidBlocks.add(entry) else nonSolidBlocks.add(entry)
        }

        plugin.server.regionScheduler.run(plugin, world, newChunkX, newChunkZ) {
            for (entry in solidBlocks) {
                restoreBlock(world, entry.key, entry.value)
                region.blocks.remove(entry.key)
            }
            for (entry in nonSolidBlocks) {
                restoreBlock(world, entry.key, entry.value)
                region.blocks.remove(entry.key)
            }
            future.complete(null)
        }
        
        return future
    }

    private fun restoreBlock(world: org.bukkit.World, packedCoord: Long, cached: CachedBlock) {
        val x = BorderRegion.unpackX(packedCoord)
        val y = BorderRegion.unpackY(packedCoord)
        val z = BorderRegion.unpackZ(packedCoord)

        val block = world.getBlockAt(x, y, z)
        val blockData = Bukkit.createBlockData(cached.blockData)
        
        // setBlockData with applyPhysics = false to avoid updating neighbors just in case
        block.setBlockData(blockData, false)

        val state = block.state

        if (state is Container && cached.inventoryBase64 != null) {
            val items = itemsFromBase64(cached.inventoryBase64)
            if (items != null) {
                state.inventory.contents = items
            }
        } else if (state is Sign && cached.signLines != null) {
            val frontSide = state.getSide(org.bukkit.block.sign.Side.FRONT)
            for (i in 0..3) {
                if (i < cached.signLines.size) {
                    frontSide.setLine(i, cached.signLines[i])
                }
            }
            if (cached.signFrontColor != null) frontSide.color = org.bukkit.DyeColor.getByColor(Color.fromRGB(cached.signFrontColor)) ?: org.bukkit.DyeColor.BLACK
            if (cached.signFrontGlowing != null) frontSide.isGlowingText = cached.signFrontGlowing

            val backSide = state.getSide(org.bukkit.block.sign.Side.BACK)
            if (cached.signBackColor != null) backSide.color = org.bukkit.DyeColor.getByColor(Color.fromRGB(cached.signBackColor)) ?: org.bukkit.DyeColor.BLACK
            if (cached.signBackGlowing != null) backSide.isGlowingText = cached.signBackGlowing
        }

        state.update(true, false)
    }

    private fun itemsFromBase64(data: String): Array<ItemStack?>? {
        return try {
            val inputStream = ByteArrayInputStream(Base64.getDecoder().decode(data))
            val dataInput = BukkitObjectInputStream(inputStream)
            val size = dataInput.readInt()
            val items = arrayOfNulls<ItemStack>(size)
            for (i in 0 until size) {
                items[i] = dataInput.readObject() as ItemStack?
            }
            dataInput.close()
            items
        } catch (e: Exception) {
            service.logger.log("Failed to deserialize inventory: ${e.message}")
            null
        }
    }
}

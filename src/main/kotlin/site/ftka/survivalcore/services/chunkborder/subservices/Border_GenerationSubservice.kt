package site.ftka.survivalcore.services.chunkborder.subservices

import org.bukkit.Material
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chunkborder.ChunkBorderService
import site.ftka.survivalcore.services.chunkborder.objects.BorderRegion
import java.util.concurrent.CompletableFuture

class Border_GenerationSubservice(private val service: ChunkBorderService, private val plugin: MClass) {

    fun generateChunkEdges(world: org.bukkit.World, region: BorderRegion, newChunkX: Int, newChunkZ: Int): CompletableFuture<Void> {
        val cx = newChunkX shl 4
        val cz = newChunkZ shl 4

        val coords = mutableListOf<Pair<Int, Int>>()

        // Iterate the entire outer rim (x: -1 to 16, z: -1 to 16)
        for (x in -1..16) {
            for (z in -1..16) {
                // We only care about the rim
                if (x == -1 || x == 16 || z == -1 || z == 16) {
                    val absoluteX = cx + x
                    val absoluteZ = cz + z
                    
                    // What chunk does this block geometrically fall into?
                    val targetChunkX = absoluteX shr 4
                    val targetChunkZ = absoluteZ shr 4

                    // **CRITICAL FIX**: If the block geometrically resides inside a chunk that is ALREADY UNLOCKED, 
                    // it means it's part of the playable area and must NOT be converted to glass!
                    // This elegantly prevents rogue inner columns and diagonal corner overlaps.
                    if (region.unlockedChunks.contains(Pair(targetChunkX, targetChunkZ))) {
                        continue
                    }

                    coords.add(Pair(x, z))
                }
            }
        }

        if (coords.isEmpty()) return CompletableFuture.completedFuture(null)

        // Group absolute coordinates by the chunk they fall into
        val chunkCoordGroups = coords.groupBy { Pair((cx + it.first) shr 4, (cz + it.second) shr 4) }
        val futures = mutableListOf<CompletableFuture<Void>>()

        for ((targetChunk, chunkCoords) in chunkCoordGroups) {
            val targetChunkX = targetChunk.first
            val targetChunkZ = targetChunk.second
            
            val future = CompletableFuture<Void>()
            futures.add(future)
            
            plugin.server.regionScheduler.run(plugin, world, targetChunkX, targetChunkZ) {
                for (coord in chunkCoords) {
                    val x = cx + coord.first
                    val z = cz + coord.second
                    for (y in -64..319) {
                        val block = world.getBlockAt(x, y, z)
                        
                        // Don't replace bedrock
                        if (block.type == Material.BEDROCK) continue
                        
                        // Serialize the block first
                        val packed = BorderRegion.packCoord(x, y, z)
                        if (!region.blocks.containsKey(packed)) {
                            val cached = service.storage_ss.serializeBlock(block)
                            region.blocks[packed] = cached
                            block.setType(Material.GRAY_STAINED_GLASS, false)
                        }
                    }
                }
                future.complete(null)
            }
        }
        
        return CompletableFuture.allOf(*futures.toTypedArray())
    }
}

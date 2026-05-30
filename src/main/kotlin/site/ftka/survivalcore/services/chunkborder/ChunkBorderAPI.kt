package site.ftka.survivalcore.services.chunkborder

import org.bukkit.World
import site.ftka.survivalcore.services.chunkborder.objects.BorderRegion
import java.util.concurrent.CompletableFuture

class ChunkBorderAPI(private val service: ChunkBorderService) {

    fun createRegion(id: String, world: World, startChunkX: Int, startChunkZ: Int): CompletableFuture<BorderRegion> {
        val region = BorderRegion(id)
        region.unlockedChunks.add(Pair(startChunkX, startChunkZ))
        return service.generation_ss.generateChunkEdges(world, region, startChunkX, startChunkZ).thenApply { region }
    }

    fun expandRegion(world: World, region: BorderRegion, newChunkX: Int, newChunkZ: Int): CompletableFuture<Void> {
        if (region.unlockedChunks.contains(Pair(newChunkX, newChunkZ))) {
            return CompletableFuture.completedFuture(null)
        }
        region.unlockedChunks.add(Pair(newChunkX, newChunkZ))
        return service.restoration_ss.restoreSharedEdges(world, region, newChunkX, newChunkZ).thenCompose {
            service.generation_ss.generateChunkEdges(world, region, newChunkX, newChunkZ)
        }
    }

    fun destroyRegion(world: World, region: BorderRegion): CompletableFuture<Void> {
        return service.restoration_ss.restoreRegion(world, region)
    }
}

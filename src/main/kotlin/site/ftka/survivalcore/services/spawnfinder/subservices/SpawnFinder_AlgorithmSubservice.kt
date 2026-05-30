package site.ftka.survivalcore.services.spawnfinder.subservices

import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.command.CommandSender
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import site.ftka.survivalcore.services.spawnfinder.SpawnFinderService
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Semaphore
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

class SpawnFinder_AlgorithmSubservice(private val service: SpawnFinderService, private val plugin: MClass) {

    private val forbiddenBiomes = setOf(
        Biome.OCEAN, Biome.DEEP_OCEAN, Biome.WARM_OCEAN, Biome.LUKEWARM_OCEAN, Biome.DEEP_LUKEWARM_OCEAN,
        Biome.COLD_OCEAN, Biome.DEEP_COLD_OCEAN, Biome.FROZEN_OCEAN, Biome.DEEP_FROZEN_OCEAN,
        Biome.RIVER, Biome.FROZEN_RIVER,
        Biome.MUSHROOM_FIELDS
    )

    private val foodBlocks = setOf(
        Material.HAY_BLOCK, Material.PUMPKIN, Material.MELON, Material.SWEET_BERRY_BUSH,
        Material.CARROTS, Material.POTATOES, Material.BEETROOTS, Material.WHEAT,
        Material.COCOA, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM,
        Material.BROWN_MUSHROOM_BLOCK, Material.RED_MUSHROOM_BLOCK, Material.CAKE,
        Material.KELP, Material.KELP_PLANT, Material.CAVE_VINES, Material.CAVE_VINES_PLANT,
        Material.CHORUS_PLANT, Material.CHORUS_FLOWER, Material.OAK_LEAVES, Material.DARK_OAK_LEAVES,
        Material.BEEHIVE, Material.BEE_NEST
    )

    @Volatile
    var isAnalysing = false
        private set

    @Volatile
    private var cancelRequested = false

    private var activeBossBar: BossBar? = null
    private var originalSender: CommandSender? = null

    fun cancel(sender: CommandSender) {
        val mm = MiniMessage.miniMessage()
        if (!isAnalysing) {
            sender.sendMessage(mm.deserialize("<red>✖ No active analysis to cancel.</red>"))
            return
        }
        cancelRequested = true
        isAnalysing = false
        
        val origSender = originalSender
        activeBossBar?.let { bossBar ->
            if (origSender is Player) {
                origSender.hideBossBar(bossBar)
            }
        }
        activeBossBar = null
        originalSender = null
        
        sender.sendMessage(mm.deserialize("<green>✔ Analysis cancelled successfully.</green>"))
        if (origSender != null && origSender != sender) {
            origSender.sendMessage(mm.deserialize("<red>⚠ Analysis was cancelled by another user.</red>"))
        }
    }

    fun analyse(sender: CommandSender, radius: Int) {
        val mm = MiniMessage.miniMessage()
        if (isAnalysing) {
            sender.sendMessage(mm.deserialize("<red>✖ An analysis is already in progress. Use <white>/randomspawn cancel</white> to stop it.</red>"))
            return
        }
        
        isAnalysing = true
        cancelRequested = false
        originalSender = sender
        
        sender.sendMessage(mm.deserialize("<yellow>Starting analysis for radius <white>$radius</white>... This might take a while.</yellow>"))
        service.logger.log("Starting analysis for radius $radius", LogLevel.LOW)
        
        CompletableFuture.runAsync {
            val step = 4 // Check every 4th chunk
            
            val coordsToCheck = mutableListOf<Pair<Int, Int>>()
            for (x in -radius..radius step step) {
                for (z in -radius..radius step step) {
                    if (isValidDistance(x, z)) {
                        coordsToCheck.add(Pair(x, z))
                    }
                }
            }
            
            val totalCells = coordsToCheck.size
            sender.sendMessage(mm.deserialize("<green>✔ Found <white>$totalCells</white> potential chunks to analyze. Dispatching tasks...</green>"))
            
            if (totalCells == 0) {
                isAnalysing = false
                originalSender = null
                return@runAsync
            }

            val chunksFound = AtomicInteger(0)
            val chunksProcessed = AtomicInteger(0)
            val semaphore = Semaphore(150) // Limit active chunk generation tasks to 150 to prevent queue overload and memory spikes
            
            val timestampQueue = java.util.concurrent.ConcurrentLinkedQueue<Long>()
            val queueSize = AtomicInteger(0)

            val bossBar = BossBar.bossBar(
                mm.deserialize("<yellow>Analyzing spawns...: <white>0 / $totalCells (0.00%) | 0.0 c/s | ETA: Calculating...</white></yellow>"),
                0.0f,
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS
            )
            activeBossBar = bossBar
            
            if (sender is Player) {
                sender.showBossBar(bossBar)
            }

            val startTime = System.currentTimeMillis()

            for (coord in coordsToCheck) {
                if (cancelRequested) {
                    break
                }
                try {
                    semaphore.acquire()
                } catch (e: InterruptedException) {
                    break
                }
                if (cancelRequested) {
                    semaphore.release()
                    break
                }
                checkChunkAsync(coord.first, coord.second) { isValid ->
                    val processed = chunksProcessed.incrementAndGet()
                    if (!cancelRequested) {
                        if (isValid) {
                            if (!service.validSpawns.contains(coord)) {
                                service.validSpawns.add(coord)
                                val count = chunksFound.incrementAndGet()
                                service.logger.log("Found valid spawn at ${coord.first}, ${coord.second} (Total: $count)", LogLevel.LOW)
                            }
                        } else {
                            if (service.validSpawns.remove(coord)) {
                                service.logger.log("Removed now-invalid spawn at ${coord.first}, ${coord.second} from the pool.", LogLevel.LOW)
                            }
                        }
                        
                        val progress = processed.toFloat() / totalCells.toFloat()
                        bossBar.progress(progress.coerceIn(0.0f, 1.0f))
                        
                        val percentageStr = String.format(java.util.Locale.US, "%.2f", progress * 100)
                        
                        val now = System.currentTimeMillis()
                        timestampQueue.add(now)
                        val qSize = queueSize.incrementAndGet()
                        if (qSize > 200) {
                            timestampQueue.poll()
                            queueSize.decrementAndGet()
                        }
                        
                        val elapsedGlobal = now - startTime
                        val firstTime = timestampQueue.peek() ?: startTime
                        val elapsedWindow = now - firstTime
                        val windowSize = queueSize.get()
                        
                        val chunksPerSec = if (windowSize > 1 && elapsedWindow > 0) {
                            (windowSize - 1).toDouble() / elapsedWindow * 1000.0
                        } else if (elapsedGlobal > 0) {
                            processed.toDouble() / elapsedGlobal * 1000.0
                        } else {
                            0.0
                        }
                        
                        val csStr = String.format(java.util.Locale.US, "%.1f", chunksPerSec)
                        
                        val etaStr = if (chunksPerSec > 0.0) {
                            val remainingChunks = totalCells - processed
                            val remainingTimeMs = (remainingChunks.toDouble() / chunksPerSec * 1000.0).toLong()
                            formatDuration(remainingTimeMs)
                        } else {
                            "Calculating..."
                        }
                        
                        bossBar.name(mm.deserialize("<yellow>Analyzing spawns...: <white>$processed / $totalCells</white> | <color:#ffcc00>($percentageStr%)</color> | <color:#ff9900>$csStr c/s</color> | <color:#00ffcc>ETA: $etaStr</color></yellow>"))
                        
                        if (processed == totalCells) {
                            if (sender is Player) sender.hideBossBar(bossBar)
                            service.storage_ss.saveAsync()
                            sender.sendMessage(mm.deserialize("<green>✔ Analysis complete! Found <white>${chunksFound.get()}</white> new valid spawns.</green>"))
                            isAnalysing = false
                            activeBossBar = null
                            originalSender = null
                        }
                    }
                    semaphore.release()
                }
            }
        }
    }

    private fun formatDuration(ms: Long): String {
        if (ms <= 0) return "0s"
        val totalSecs = ms / 1000
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60
        
        val sb = StringBuilder()
        if (hours > 0) sb.append("${hours}h")
        if (minutes > 0 || hours > 0) sb.append("${minutes}m")
        sb.append("${seconds}s")
        return sb.toString()
    }

    private fun isValidDistance(x: Int, z: Int): Boolean {
        for (claim in plugin.servicesFwk.territory.claims.keys) {
            val distSq = (claim.first - x) * (claim.first - x) + (claim.second - z) * (claim.second - z)
            if (distSq <= 48 * 48) return false
        }
        return true
    }

    private fun checkChunkAsync(x: Int, z: Int, callback: (Boolean) -> Unit) {
        val world = plugin.server.worlds.firstOrNull { it.environment == org.bukkit.World.Environment.NORMAL } ?: return callback(false)
        
        plugin.server.regionScheduler.execute(plugin, world, x, z) {
            val blockX = x * 16 + 8
            val blockZ = z * 16 + 8
            val biome = world.getBiome(blockX, 64, blockZ)
            if (forbiddenBiomes.contains(biome)) {
                return@execute callback(false)
            }

            world.getChunkAtAsync(x, z).thenAccept { chunk ->
                // Check single chunk for trees and food
                var foundTree = false
                var foundFood = false

                // Simple scan strategy: scan surface blocks from Y=100 down to Y=63 (sea level)
                for (bx in 0..15 step 3) {
                    for (bz in 0..15 step 3) {
                        for (by in 100 downTo 63) {
                            val mat = chunk.getBlock(bx, by, bz).type
                            if (mat.name.endsWith("_LOG")) foundTree = true
                            if (foodBlocks.contains(mat)) foundFood = true
                            if (foundTree && foundFood) break
                        }
                        if (foundTree && foundFood) break
                    }
                    if (foundTree && foundFood) break
                }
                callback(foundTree && foundFood)
            }.exceptionally {
                callback(false)
                null
            }
        }
    }
}

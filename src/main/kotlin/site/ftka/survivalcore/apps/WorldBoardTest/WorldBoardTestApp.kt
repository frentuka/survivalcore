package site.ftka.survivalcore.apps.WorldBoardTest

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.WorldBoardTest.commands.WorldBoardTestApp_TestCommand
import site.ftka.survivalcore.apps.WorldBoardTest.listeners.WorldBoardTestApp_Listener
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import site.ftka.survivalcore.services.chunkborder.objects.BorderRegion
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class WorldBoardTestApp(private val plugin: MClass) {
    internal val logger = plugin.loggingInitless.getLog("WorldBoardTestApp", Component.text("WorldBoardTest").color(NamedTextColor.LIGHT_PURPLE))

    // Manage active test sessions per player. Pair<BorderRegion, ScheduledTask?>
    val activeRegions = ConcurrentHashMap<UUID, Pair<BorderRegion, ScheduledTask?>>()
    
    // Store the WorldBoard IDs for each player's active wall segments
    // Key: UUID. Value: Map of Segment ID (String) -> Board ID (String)
    val activeSegmentBoards = ConcurrentHashMap<UUID, ConcurrentHashMap<String, String>>()

    internal val listener = WorldBoardTestApp_Listener(this, plugin)
    private var proximityTask: ScheduledTask? = null

    fun init() {
        logger.log("Initializing...", LogLevel.LOW)
        plugin.getCommand("wbtest")?.setExecutor(WorldBoardTestApp_TestCommand(plugin, this))
        plugin.propEventsInitless.registerListener(listener)
        
        // Start proximity tracking loop
        proximityTask = plugin.server.globalRegionScheduler.runAtFixedRate(plugin, { _ ->
            tickProximity()
        }, 10L, 10L)
    }

    private fun tickProximity() {
        for ((uuid, pair) in activeRegions) {
            val player = plugin.server.getPlayer(uuid) ?: continue
            val region = pair.first
            val playerLoc = player.location
            val targetY = playerLoc.blockY + 2.0

            val currentBoards = activeSegmentBoards.computeIfAbsent(uuid) { ConcurrentHashMap() }
            val activeSegmentsThisTick = mutableSetOf<String>()

            // Find all locked walls facing unlocked chunks
            for (unlocked in region.unlockedChunks) {
                val cx = unlocked.first
                val cz = unlocked.second

                // Check 4 cardinal directions
                val segments = listOf(
                    Triple(cx, cz - 1, "NORTH"),
                    Triple(cx, cz + 1, "SOUTH"),
                    Triple(cx - 1, cz, "WEST"),
                    Triple(cx + 1, cz, "EAST")
                )

                for (seg in segments) {
                    if (!region.unlockedChunks.contains(Pair(seg.first, seg.second))) {
                        // This adjacent chunk is locked! It is a border wall.
                        val segId = "${unlocked.first},${unlocked.second},${seg.third}"
                        activeSegmentsThisTick.add(segId)

                        // Calculate center of this specific wall segment
                        val wallCenterX = unlocked.first * 16.0 + when (seg.third) {
                            "WEST" -> 0.0
                            "EAST" -> 16.0
                            else -> 8.0
                        }
                        val wallCenterZ = unlocked.second * 16.0 + when (seg.third) {
                            "NORTH" -> 0.0
                            "SOUTH" -> 16.0
                            else -> 8.0
                        }

                        // Calculate 2D distance to player
                        val dx = playerLoc.x - wallCenterX
                        val dz = playerLoc.z - wallCenterZ
                        val distSq = dx * dx + dz * dz

                        if (distSq <= 100.0) { // ~10 blocks
                            val boardId = currentBoards[segId]
                            if (boardId == null) {
                                // Spawn new board
                                val newBoardId = "wbtest_${uuid}_$segId"
                                val spawnLoc = org.bukkit.Location(player.world, wallCenterX, targetY, wallCenterZ)
                                
                                val yaw = when (seg.third) {
                                    "NORTH" -> 0f
                                    "SOUTH" -> 180f
                                    "WEST" -> 270f
                                    "EAST" -> 90f
                                    else -> 0f
                                }
                                spawnLoc.yaw = yaw

                                plugin.servicesFwk.worldBoard.api.createBoard(newBoardId, spawnLoc, Component.text("Punch to expand!").color(NamedTextColor.AQUA)) {
                                    billboardSetting = org.bukkit.entity.Display.Billboard.FIXED
                                    backgroundColorARGB = org.bukkit.Color.fromARGB(0, 0, 0, 0)
                                    frame = site.ftka.survivalcore.services.worldboard.objects.WorldBoardFrame.ROUNDED
                                    scale = org.joml.Vector3f(2.5f, 2.5f, 2.5f)
                                    animation = site.ftka.survivalcore.services.worldboard.objects.WorldBoardAnimation.FADE
                                }

                                currentBoards[segId] = newBoardId
                            } else {
                                // Board exists, check height snap
                                val board = plugin.servicesFwk.worldBoard.api.getBoard(boardId)
                                if (board != null) {
                                    // Wait, we can't easily read its current Y from the API safely across threads.
                                    // But we CAN just always issue a teleport if we track the targetY locally, or just issue the teleport!
                                    // Display entity teleports are cheap. To be optimized, we could store lastY.
                                    val loc = org.bukkit.Location(player.world, wallCenterX, targetY, wallCenterZ)
                                    val yaw = when (seg.third) {
                                        "NORTH" -> 0f
                                        "SOUTH" -> 180f
                                        "WEST" -> 270f
                                        "EAST" -> 90f
                                        else -> 0f
                                    }
                                    loc.yaw = yaw
                                    board.teleport(loc, 10) // Smooth Y movement over 10 ticks
                                }
                            }
                        } else {
                            // Out of range, remove if exists
                            val boardId = currentBoards.remove(segId)
                            if (boardId != null) {
                                plugin.servicesFwk.worldBoard.api.removeBoard(boardId)
                            }
                        }
                    }
                }
            }

            // Cleanup any boards that are no longer valid segments (e.g. wall was destroyed)
            val segmentsToRemove = currentBoards.keys.filter { it !in activeSegmentsThisTick }
            for (seg in segmentsToRemove) {
                val boardId = currentBoards.remove(seg)
                if (boardId != null) {
                    plugin.servicesFwk.worldBoard.api.removeBoard(boardId)
                }
            }
        }
    }

    fun restart() {
        logger.log("Restarting...", LogLevel.LOW)
        stop()
        init()
    }

    fun cleanupPlayer(uuid: UUID) {
        val playerBoards = activeSegmentBoards.remove(uuid)
        if (playerBoards != null) {
            for (boardId in playerBoards.values) {
                plugin.servicesFwk.worldBoard.api.removeBoard(boardId)
            }
        }
    }

    fun stop() {
        logger.log("Stopping...", LogLevel.LOW)
        proximityTask?.cancel()
        proximityTask = null
        
        // Cleanup all active tests
        for ((uuid, pair) in activeRegions) {
            pair.second?.cancel()
            val player = plugin.server.getPlayer(uuid)
            if (player != null) {
                plugin.servicesFwk.chunkBorder.api.destroyRegion(player.world, pair.first)
            }
            cleanupPlayer(uuid)
        }
        activeRegions.clear()
        plugin.propEventsInitless.unregisterListener(listener)
    }
}

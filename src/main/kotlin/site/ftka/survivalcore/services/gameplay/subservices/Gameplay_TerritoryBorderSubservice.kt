package site.ftka.survivalcore.services.gameplay.subservices

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import site.ftka.survivalcore.initless.proprietaryEvents.annotations.PropEventHandler
import site.ftka.survivalcore.initless.proprietaryEvents.events.ChunkClaimedEvent
import site.ftka.survivalcore.services.chunkborder.events.BorderPunchEvent
import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropListener
import site.ftka.survivalcore.services.chunkborder.objects.BorderRegion
import site.ftka.survivalcore.services.gameplay.GameplayService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRegisterEvent
import site.ftka.survivalcore.services.playerdata.events.PlayerDataUnregisterEvent
import site.ftka.survivalcore.services.gameplay.gui.Gameplay_BorderExpansionGUI
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import site.ftka.survivalcore.services.gameplay.objects.PriceCalculator
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope

class Gameplay_TerritoryBorderSubservice(private val service: GameplayService, private val plugin: MClass) : PropListener {

    private val logger = service.logger.sub("TerritoryBorder")

    // Active regions loaded in memory
    val activeRegions = ConcurrentHashMap<UUID, BorderRegion>()
    
    // Store the WorldBoard IDs for each player's active wall segments
    // Key: UUID. Value: Map of Segment ID (String) -> Board ID (String)
    val activeSegmentBoards = ConcurrentHashMap<UUID, ConcurrentHashMap<String, String>>()
    val displayedCycleIndex = ConcurrentHashMap<String, Int>()

    private var proximityTask: ScheduledTask? = null

    fun init() {
        plugin.propEventsInitless.registerListener(this)
        
        // Start proximity tracking loop
        proximityTask = plugin.server.globalRegionScheduler.runAtFixedRate(plugin, { _ ->
            tickProximity()
        }, 2L, 2L)
    }

    fun restart() {
        plugin.propEventsInitless.unregisterListener(this)
        plugin.propEventsInitless.registerListener(this)
    }

    fun stop() {
        plugin.propEventsInitless.unregisterListener(this)
        proximityTask?.cancel()
        proximityTask = null
        
        for (uuid in activeSegmentBoards.keys) {
            cleanupPlayerBoards(uuid)
        }
        activeRegions.clear()
    }

    @PropEventHandler
    fun onPlayerRegister(event: PlayerDataRegisterEvent) {
        val region = event.playerdata?.borderRegion
        if (region != null) {
            activeRegions[event.uuid] = region
            
            // Automatically migrate existing physical blocks to the new default material
            val player = plugin.server.getPlayer(event.uuid)
            if (player != null) {
                plugin.servicesFwk.chunkBorder.api.refreshRegionBorders(player.world, region, org.bukkit.Material.LIGHT_GRAY_STAINED_GLASS)
            }
        }
    }

    @PropEventHandler
    fun onPlayerUnregister(event: PlayerDataUnregisterEvent) {
        activeRegions.remove(event.uuid)
        cleanupPlayerBoards(event.uuid)
    }

    @PropEventHandler
    fun onBorderPunch(event: BorderPunchEvent) {
        val player = event.player
        val block = event.clickedBlock
        
        // Target chunk is the geometrical chunk the border block sits in
        val chunkX = block.x shr 4
        val chunkZ = block.z shr 4

        // Get the list of unlocked chunks owned by the player
        val region = activeRegions[player.uniqueId] ?: return
        val pdata = plugin.servicesFwk.playerData.api.getPlayerData_locally(player.uniqueId)
        val unlockedChunks = pdata?.unlockedChunks ?: region.unlockedChunks.toList()

        // Open the Confirm GUI
        val gui = Gameplay_BorderExpansionGUI(plugin, player, chunkX, chunkZ, unlockedChunks)
        player.openInventory(gui.inventory)
    }

    @PropEventHandler
    fun onChunkClaimed(event: ChunkClaimedEvent) {
        // Trigger border API to physically generate or expand the region
        val uuid = event.playerUuid

        val player = plugin.server.getPlayer(uuid) ?: return

        // Access PlayerData asynchronously to read/write borderRegion
        GlobalScope.launch {
            plugin.servicesFwk.playerData.inout_ss.makeModification(uuid) { data ->
                var region = data.borderRegion
                if (region == null) {
                    // First time creating a region for this player
                    // Note: CreateRegion generates the chunks and updates the object natively
                    val future = plugin.servicesFwk.chunkBorder.api.createRegion("border_$uuid", player.world, event.chunkX, event.chunkZ)
                    region = future.join()
                    data.borderRegion = region
                } else {
                    // Expand existing region
                    val future = plugin.servicesFwk.chunkBorder.api.expandRegion(player.world, region, event.chunkX, event.chunkZ)
                    future.join()
                }

                // Update active tracking
                activeRegions[uuid] = region
                
                true
            }
        }
    }

    private fun tickProximity() {
        for ((uuid, region) in activeRegions) {
            val player = plugin.server.getPlayer(uuid) ?: continue
            val playerLoc = player.location
            val targetY = playerLoc.blockY + 1.0
            val pdata = plugin.servicesFwk.playerData.api.getPlayerData_locally(uuid)
            val unlockedChunks = pdata?.unlockedChunks ?: region.unlockedChunks.toList()

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
                                val options = site.ftka.survivalcore.services.gameplay.objects.PriceCalculator.calculateChunkPrice(player.world, unlockedChunks, seg.first, seg.second)
                                val cycleIntervalSeconds = 8
                                val cycleIndex = ((System.currentTimeMillis() / 1000) / cycleIntervalSeconds % options.size).toInt()
                                val currentOption = options[cycleIndex]

                                val layout = site.ftka.survivalcore.services.worldboard.objects.WorldBoardTextLayout()
                                layout.append(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<italic><gradient:#e69500:#a86300>Locked region</gradient></italic>"))
                                layout.newLine()
                                layout.newLine()
                                
                                val pagination = buildString {
                                    append("[")
                                    for (i in options.indices) {
                                        if (i == cycleIndex) append("<bold><green>I</green></bold>")
                                        else append("<gray>I</gray>")
                                    }
                                    append("]")
                                }
                                layout.append(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(pagination)).append(Component.text(" "))
                                
                                for ((index, cost) in currentOption.items.withIndex()) {
                                    layout.append(Component.text("${cost.amount}x ").color(NamedTextColor.WHITE))
                                    // Use higher vertical offset since user mentioned they are 1/4th of a block too low.
                                    val isSolid = cost.type.isSolid
                                    val dynamicZOffset = if (isSolid) -0.03f else 0.0f
                                    layout.appendIcon(cost, verticalOffset = 0.12f, scaleMultiplier = 0.23f, zOffset = dynamicZOffset)
                                    if (index < currentOption.items.size - 1) {
                                        layout.append(Component.text(", ").color(NamedTextColor.WHITE))
                                    }
                                }

                                var subtitleComp: Component? = null
                                if (currentOption.discountDescription != null) {
                                    subtitleComp = Component.text(currentOption.discountDescription)
                                        .color(NamedTextColor.GREEN)
                                        .decorate(net.kyori.adventure.text.format.TextDecoration.ITALIC)
                                }

                                val boardId = currentBoards[segId]
                                if (boardId == null) {
                                    // Spawn new board
                                    val newBoardId = "border_${uuid}_$segId"
                                    val spawnLoc = org.bukkit.Location(player.world, wallCenterX, targetY, wallCenterZ)
                                    
                                    val yaw = when (seg.third) {
                                        "NORTH" -> 0f
                                        "SOUTH" -> 180f
                                        "WEST" -> 270f
                                        "EAST" -> 90f
                                        else -> 0f
                                    }
                                    spawnLoc.yaw = yaw

                                    plugin.servicesFwk.worldBoard.api.createBoard(newBoardId, spawnLoc, layout.buildComponent()) {
                                        billboardSetting = org.bukkit.entity.Display.Billboard.FIXED
                                        backgroundColorARGB = org.bukkit.Color.fromARGB(0, 0, 0, 0)
                                        frame = site.ftka.survivalcore.services.worldboard.objects.WorldBoardFrame.ROUNDED
                                        frameGradient = "<gradient:#e69500:#a86300>"
                                        scale = org.joml.Vector3f(1.8f, 1.8f, 1.8f)
                                        icons = layout.buildIcons(frame)
                                        animation = site.ftka.survivalcore.services.worldboard.objects.WorldBoardAnimation.POP
                                        subtitle = subtitleComp
                                    }

                                    currentBoards[segId] = newBoardId
                                    displayedCycleIndex[newBoardId] = cycleIndex
                                } else {
                                    // Board exists, check height snap and update content
                                    val board = plugin.servicesFwk.worldBoard.api.getBoard(boardId)
                                    if (board != null) {
                                        val loc = org.bukkit.Location(player.world, wallCenterX, targetY, wallCenterZ)
                                        val yaw = when (seg.third) {
                                            "NORTH" -> 0f
                                            "SOUTH" -> 180f
                                            "WEST" -> 270f
                                            "EAST" -> 90f
                                            else -> 0f
                                        }
                                        loc.yaw = yaw
                                        
                                        val boardLoc = board.targetLocation ?: board.getLocation()
                                        if (boardLoc != null && boardLoc.distanceSquared(loc) > 0.1) {
                                            board.teleport(loc, 4) // Smooth native interpolation blending
                                        }
                                        
                                        // Update content only when the cycle index actually changes
                                        val currentDisplayed = displayedCycleIndex[boardId]
                                        if (currentDisplayed != cycleIndex) {
                                             board.updateContent(layout.buildComponent(), layout.buildIcons(board.frame), subtitleComp)
                                             displayedCycleIndex[boardId] = cycleIndex
                                        }
                                    }
                                }
                        } else {
                            // Out of range, remove if exists
                            val boardId = currentBoards.remove(segId)
                            if (boardId != null) {
                                plugin.servicesFwk.worldBoard.api.removeBoard(boardId)
                                displayedCycleIndex.remove(boardId)
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
                    displayedCycleIndex.remove(boardId)
                }
            }
        }
    }

    private fun cleanupPlayerBoards(uuid: UUID) {
        val playerBoards = activeSegmentBoards.remove(uuid)
        if (playerBoards != null) {
            for (boardId in playerBoards.values) {
                plugin.servicesFwk.worldBoard.api.removeBoard(boardId)
                displayedCycleIndex.remove(boardId)
            }
        }
    }
}

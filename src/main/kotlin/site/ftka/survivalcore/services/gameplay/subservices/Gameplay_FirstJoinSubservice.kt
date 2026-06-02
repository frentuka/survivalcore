package site.ftka.survivalcore.services.gameplay.subservices

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import site.ftka.survivalcore.initless.proprietaryEvents.annotations.PropEventHandler
import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropListener
import site.ftka.survivalcore.services.gameplay.GameplayService
import site.ftka.survivalcore.services.playerdata.events.PlayerDataRegisterEvent
import java.time.Duration

class Gameplay_FirstJoinSubservice(private val service: GameplayService, private val plugin: MClass) : PropListener {

    fun init() {
        plugin.propEventsInitless.registerListener(this)
    }

    fun restart() {
        plugin.propEventsInitless.unregisterListener(this)
        plugin.propEventsInitless.registerListener(this)
    }

    fun stop() {
        plugin.propEventsInitless.unregisterListener(this)
    }

    @PropEventHandler
    fun onPlayerRegister(event: PlayerDataRegisterEvent) {
        if (!event.isFirstJoin) return

        val player = plugin.server.getPlayer(event.uuid) ?: return

        service.logger.log("Player ${player.name} is joining for the first time! Initiating first join sequence.", LogLevel.NORMAL)

        // 1. Broadcast welcome message
        plugin.server.broadcast(
            Component.text("Welcome ", NamedTextColor.GRAY)
                .append(Component.text(player.name, NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text(" to the server!", NamedTextColor.GRAY))
        )

        // Give them a title as well
        val title = Title.title(
            Component.text("Welcome!", NamedTextColor.GOLD, TextDecoration.BOLD),
            Component.text("Claiming your starting territory...", NamedTextColor.YELLOW),
            Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(5000), Duration.ofMillis(1000))
        )
        player.showTitle(title)

        // 2. Obtain a valid spawn or fallback asynchronously
        getSpawnChunkAsync { spawnChunk ->
            // 3. Claim the chunk for them
            val claimed = plugin.servicesFwk.territory.claimChunk(event.uuid, spawnChunk.first, spawnChunk.second)
            if (!claimed) {
                service.logger.log("Failed to claim chunk ${spawnChunk.first}, ${spawnChunk.second} for ${player.name}. It might already be claimed.", LogLevel.HIGH, NamedTextColor.RED)
            }

            // 4. Teleport player safely and give starter item
            val world = plugin.server.worlds.first()
            val blockX = (spawnChunk.first shl 4) + 8
            val blockZ = (spawnChunk.second shl 4) + 8

            // Run teleport in chunk
            world.getChunkAtAsync(spawnChunk.first, spawnChunk.second).thenAccept { chunk ->
                val highestY = world.getHighestBlockYAt(blockX, blockZ)
                val spawnLocation = org.bukkit.Location(world, blockX.toDouble(), highestY.toDouble() + 1.0, blockZ.toDouble())
                
                player.scheduler.execute(plugin, Runnable {
                    // Give the starter item here so it's guaranteed to be on the player's region thread
                    val cake = ItemStack(Material.CAKE)
                    val meta = cake.itemMeta
                    meta.displayName(Component.text("Cake?", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                    meta.lore(listOf(
                        Component.text("Just in case of emergencies", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC)
                    ))
                    cake.itemMeta = meta
                    player.inventory.addItem(cake)

                    player.setRespawnLocation(spawnLocation, true)
                    player.teleportAsync(spawnLocation)
                }, null, 0L)
            }
        }
    }

    private fun getSpawnChunkAsync(callback: (Pair<Int, Int>) -> Unit) {
        val cached = plugin.servicesFwk.spawnFinder.validSpawns.randomOrNull()
        if (cached != null) {
            plugin.servicesFwk.spawnFinder.validSpawns.remove(cached)
            plugin.servicesFwk.spawnFinder.checkAndTriggerAnalysisIfNeeded()
            callback(cached)
            return
        }
        
        service.logger.log("No valid spawns cached in SpawnFinder! Initiating emergency radius 100 scan...", LogLevel.HIGH, NamedTextColor.RED)
        
        val world = plugin.server.worlds.first()
        val radius = 100
        val step = 4
        
        val coords = mutableListOf<Pair<Int, Int>>()
        for (x in -radius..radius step step) {
            for (z in -radius..radius step step) {
                if (plugin.servicesFwk.territory.getOwner(x, z) == null) {
                    coords.add(Pair(x, z))
                }
            }
        }
        
        coords.shuffle()
        checkNextCoord(coords, 0, world, callback)
    }

    private fun checkNextCoord(coords: List<Pair<Int, Int>>, index: Int, world: org.bukkit.World, callback: (Pair<Int, Int>) -> Unit) {
        if (index >= coords.size || index >= 100) { // Limit to 100 checks to prevent lag spikes
            service.logger.log("Emergency scan failed. Defaulting to 0,0", LogLevel.HIGH, NamedTextColor.RED)
            callback(Pair(0, 0))
            return
        }
        
        val coord = coords[index]
        plugin.server.regionScheduler.execute(plugin, world, coord.first, coord.second) {
            try {
                val blockX = coord.first shl 4
                val blockZ = coord.second shl 4
                val biome = world.getBiome(blockX, 64, blockZ)
                if (!biome.name().contains("OCEAN") && !biome.name().contains("RIVER")) {
                    callback(coord)
                } else {
                    checkNextCoord(coords, index + 1, world, callback)
                }
            } catch (e: Exception) {
                checkNextCoord(coords, index + 1, world, callback)
            }
        }
    }
}

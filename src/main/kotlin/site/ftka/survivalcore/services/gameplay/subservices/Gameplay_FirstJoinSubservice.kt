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

        // 2. Obtain a valid spawn or fallback
        var spawnChunk = plugin.servicesFwk.spawnFinder.validSpawns.randomOrNull()

        if (spawnChunk == null) {
            service.logger.log("No valid spawns cached in SpawnFinder! Initiating emergency radius 100 scan...", LogLevel.HIGH, NamedTextColor.RED)
            // Emergency scan logic
            val world = plugin.server.worlds.first() // Usually the overworld
            // Simple fast scan around 0,0 avoiding currently claimed chunks if possible, or just stepping until a valid biome is found
            spawnChunk = performEmergencyScan()
            if (spawnChunk == null) {
                service.logger.log("Emergency scan failed. Defaulting to 0,0", LogLevel.HIGH, NamedTextColor.RED)
                spawnChunk = Pair(0, 0)
            }
        } else {
            // Remove it so it doesn't get used again immediately before the territory service updates
            plugin.servicesFwk.spawnFinder.validSpawns.remove(spawnChunk)
        }

        // 3. Claim the chunk for them
        val claimed = plugin.servicesFwk.territory.claimChunk(event.uuid, spawnChunk.first, spawnChunk.second)
        if (!claimed) {
            service.logger.log("Failed to claim chunk ${spawnChunk.first}, ${spawnChunk.second} for ${player.name}. It might already be claimed.", LogLevel.HIGH, NamedTextColor.RED)
        }

        // 4. Give the starter item
        val cake = ItemStack(Material.CAKE)
        val meta = cake.itemMeta
        meta.displayName(Component.text("Cake?", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
        meta.lore(listOf(
            Component.text("Just in case of emergencies", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC)
        ))
        cake.itemMeta = meta
        player.inventory.addItem(cake)

        // 5. Teleport player safely
        val world = plugin.server.worlds.first()
        val blockX = (spawnChunk.first shl 4) + 8
        val blockZ = (spawnChunk.second shl 4) + 8

        // Run teleport in chunk
        world.getChunkAtAsync(spawnChunk.first, spawnChunk.second).thenAccept { chunk ->
            val highestY = world.getHighestBlockYAt(blockX, blockZ)
            val spawnLocation = org.bukkit.Location(world, blockX.toDouble(), highestY.toDouble() + 1.0, blockZ.toDouble())
            
            player.scheduler.execute(plugin, Runnable {
                player.setRespawnLocation(spawnLocation, true)
                player.teleportAsync(spawnLocation)
            }, null, 0L)
        }
    }

    private fun performEmergencyScan(): Pair<Int, Int>? {
        // Run a blocking-like or simple loop since this is a fallback that should never happen
        // The user said "execute an analysis with radius=100 and select the first available valid chunk found"
        // Since we are likely on an async event thread right now (Registration happens inside a coroutine GlobalScope),
        // we shouldn't do complex chunk loads synchronously if possible, but finding a chunk logic just by checking territory is fast.
        
        val world = plugin.server.worlds.first()
        val radius = 100 // chunk radius
        
        // This is a naive spiral or simple loop scan
        for (x in -radius..radius step 4) {
            for (z in -radius..radius step 4) {
                if (plugin.servicesFwk.territory.getOwner(x, z) == null) {
                    val biome = world.getBiome(x shl 4, 0, z shl 4)
                    // Basic blacklist check
                    if (!biome.name().contains("OCEAN") && !biome.name().contains("RIVER")) {
                        return Pair(x, z)
                    }
                }
            }
        }
        return null
    }
}

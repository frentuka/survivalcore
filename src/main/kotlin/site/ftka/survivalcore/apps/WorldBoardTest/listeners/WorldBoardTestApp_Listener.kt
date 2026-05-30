package site.ftka.survivalcore.apps.WorldBoardTest.listeners

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.WorldBoardTest.WorldBoardTestApp
import site.ftka.survivalcore.initless.proprietaryEvents.annotations.PropEventHandler
import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropListener
import site.ftka.survivalcore.services.chunkborder.events.BorderPunchEvent

class WorldBoardTestApp_Listener(private val app: WorldBoardTestApp, private val plugin: MClass) : PropListener {

    @PropEventHandler
    fun onBorderPunch(e: BorderPunchEvent) {
        val player = e.player
        val pair = app.activeRegions[player.uniqueId] ?: return
        
        val region = pair.first
        val oldTask = pair.second

        val clickedBlock = e.clickedBlock
        val targetChunkX = clickedBlock.x shr 4
        val targetChunkZ = clickedBlock.z shr 4

        // If it's already unlocked, ignore
        if (region.unlockedChunks.contains(Pair(targetChunkX, targetChunkZ))) return

        // Cancel old timer
        oldTask?.cancel()

        // Expand the region
        plugin.servicesFwk.chunkBorder.api.expandRegion(player.world, region, targetChunkX, targetChunkZ).thenAccept {
            // Schedule new timer
            val newTask = plugin.server.globalRegionScheduler.runDelayed(plugin, {
                app.activeRegions.remove(player.uniqueId)?.let {
                    plugin.servicesFwk.chunkBorder.api.destroyRegion(player.world, region)
                }
                app.cleanupPlayer(player.uniqueId)
            }, 400L)

            app.activeRegions[player.uniqueId] = Pair(region, newTask)
        }
    }
}

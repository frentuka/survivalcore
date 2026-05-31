package site.ftka.survivalcore.services.chunkborder.listeners

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chunkborder.ChunkBorderService
import site.ftka.survivalcore.services.chunkborder.events.BorderPunchEvent
import site.ftka.survivalcore.services.chunkborder.objects.BorderRegion

class BorderListener(private val service: ChunkBorderService, private val plugin: MClass) : Listener {

    private fun isBorderBlock(block: org.bukkit.block.Block): Boolean {
        val packed = BorderRegion.packCoord(block.x, block.y, block.z)
        val activeRegions = plugin.servicesFwk.gameplay.border_ss.activeRegions
        for (region in activeRegions.values) {
            if (region.blocks.containsKey(packed)) return true
        }
        return false
    }

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val block = e.clickedBlock ?: return
        
        // Only trigger on right-click with an empty hand
        if (e.action != Action.RIGHT_CLICK_BLOCK) return
        if (e.player.inventory.itemInMainHand.type != Material.AIR) return
        
        if (!isBorderBlock(block)) return

        // Fire proprietary event so wbtest command or other systems can handle it
        plugin.propEventsInitless.fireEvent(BorderPunchEvent(e.player, block))
    }

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        if (isBorderBlock(e.block)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onEntityExplode(e: EntityExplodeEvent) {
        val iterator = e.blockList().iterator()
        while (iterator.hasNext()) {
            val block = iterator.next()
            if (isBorderBlock(block)) {
                iterator.remove()
            }
        }
    }

    @EventHandler
    fun onBlockExplode(e: BlockExplodeEvent) {
        val iterator = e.blockList().iterator()
        while (iterator.hasNext()) {
            val block = iterator.next()
            if (isBorderBlock(block)) {
                iterator.remove()
            }
        }
    }

    @EventHandler
    fun onPistonExtend(e: BlockPistonExtendEvent) {
        for (block in e.blocks) {
            if (isBorderBlock(block)) {
                e.isCancelled = true
                return
            }
        }
    }

    @EventHandler
    fun onPistonRetract(e: BlockPistonRetractEvent) {
        for (block in e.blocks) {
            if (isBorderBlock(block)) {
                e.isCancelled = true
                return
            }
        }
    }
}

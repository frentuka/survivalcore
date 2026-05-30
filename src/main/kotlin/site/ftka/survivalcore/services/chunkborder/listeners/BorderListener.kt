package site.ftka.survivalcore.services.chunkborder.listeners

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chunkborder.ChunkBorderService
import site.ftka.survivalcore.services.chunkborder.events.BorderPunchEvent

class BorderListener(private val service: ChunkBorderService, private val plugin: MClass) : Listener {

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val block = e.clickedBlock ?: return
        
        // Check if the block is a Gray Stained Glass
        if (block.type != Material.GRAY_STAINED_GLASS) return
        
        // A player could left or right click. The user specified "Any. It's just a test"
        if (e.action != Action.LEFT_CLICK_BLOCK && e.action != Action.RIGHT_CLICK_BLOCK) return

        // Fire proprietary event so wbtest command or other systems can handle it
        plugin.propEventsInitless.fireEvent(BorderPunchEvent(e.player, block))
    }
}

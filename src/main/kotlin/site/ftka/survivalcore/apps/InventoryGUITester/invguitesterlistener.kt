package site.ftka.survivalcore.apps.InventoryGUITester

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import site.ftka.survivalcore.MClass

class invguitesterlistener(private val app: invguitester, private val plugin: MClass): Listener {

    @EventHandler
    fun onJump(event: PlayerJumpEvent) {
        val player = event.player
        val owner = app.invgowner
        val invtype = InventoryType.BARREL
        val title = Component.text("XD").color(NamedTextColor.DARK_AQUA)
        val inv = plugin.servicesCore.inventoryGUIService.createInventory(owner, invtype, title)

        val itemS = ItemStack(Material.ACACIA_BOAT, 5)
        itemS.displayName().append(Component.text("XDD").color(NamedTextColor.RED))
        inv.setItem(10, itemS)

        player.openInventory(inv)
    }

}
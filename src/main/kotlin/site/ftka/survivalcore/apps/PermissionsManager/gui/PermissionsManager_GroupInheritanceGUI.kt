package site.ftka.survivalcore.apps.PermissionsManager.gui

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.inventorygui.interfaces.InventoryGUIOwner
import site.ftka.survivalcore.services.permissions.objects.PermissionGroup
import java.util.UUID

internal class PermissionsManager_GroupInheritanceGUI(
    private val plugin: MClass,
    private val player: Player,
    private val groupUUID: UUID
) : InventoryGUIOwner {

    override val ownerName = "PermissionsManager_GroupInherit_${player.uniqueId}"
    private val inv: Inventory
    private val mm = MiniMessage.miniMessage()
    private val permsAPI = plugin.servicesFwk.permissions.api
    private var groupsList = listOf<PermissionGroup>()

    private val contentSlots = listOf(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    )

    init {
        val group = permsAPI.getGroup(groupUUID)
        val name = group?.displayName ?: "Group"
        val title = mm.deserialize("<#8298d9><bold>$name Inheritances</bold></#8298d9>")
        inv = plugin.servicesFwk.inventoryGUI.api.createInventory(this, 54, title)
        setupItems()
    }

    private fun createItem(material: Material, name: String, vararg lore: String): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item
        meta.displayName(mm.deserialize(name))
        meta.lore(lore.map { mm.deserialize(it) })
        item.itemMeta = meta
        return item
    }

    override fun getInventory(): Inventory = inv

    private fun setupItems() {
        inv.clear()

        val filler = createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray> </gray>")
        for (i in 0 until 54) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, filler)
            }
        }

        val group = permsAPI.getGroup(groupUUID)
        if (group == null) {
            inv.setItem(22, createItem(Material.BARRIER, "<red>Group not found</red>"))
            return
        }

        // Get all groups except this group
        groupsList = permsAPI.getGroups()
            .filter { it.uuid != groupUUID }
            .sortedBy { it.name }

        for (index in groupsList.indices) {
            if (index >= contentSlots.size) break
            val target = groupsList[index]
            val slot = contentSlots[index]

            val inherits = group.inheritances.contains(target.uuid)
            val mat = if (inherits) Material.LIME_DYE else Material.GRAY_DYE
            val stateStr = if (inherits) "<green>✔ Inheriting</green>" else "<red>✖ Not Inherited</red>"

            val targetItem = createItem(
                mat,
                "<${target.primaryColor}><b>${target.displayName}</b></${target.primaryColor}>",
                "<gray>System Name: <white>${target.name}</white></gray>",
                "<gray>Inheritance Status: $stateStr</gray>",
                "",
                "<yellow>Click to toggle inheritance</yellow>"
            )
            inv.setItem(slot, targetItem)
        }

        // Back button
        inv.setItem(45, createItem(Material.ARROW, "<red>Back to Details</red>"))
    }

    override fun clickEvent(event: InventoryClickEvent) {
        val clickedInv = event.clickedInventory ?: return
        if (clickedInv == inv) {
            event.isCancelled = true

            val group = permsAPI.getGroup(groupUUID) ?: return
            val slot = event.slot

            if (slot == 45) {
                player.scheduler.execute(plugin, {
                    player.closeInventory()
                    val gui = PermissionsManager_GroupDetailGUI(plugin, player, groupUUID)
                    player.openInventory(gui.inventory)
                }, null, 0L)
                return
            }

            val clickedIndex = contentSlots.indexOf(slot)
            if (clickedIndex != -1 && clickedIndex < groupsList.size) {
                val targetGroup = groupsList[clickedIndex]
                
                player.scheduler.execute(plugin, {
                    val inherits = group.inheritances.contains(targetGroup.uuid)
                    if (inherits) {
                        permsAPI.group_removeInheritance(group.name, targetGroup.name)
                        plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<green>✔ Removed inheritance of '${targetGroup.name}' from '${group.name}'.</green>"))
                    } else {
                        permsAPI.group_addInheritance(group.name, targetGroup.name)
                        plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<green>✔ Group '${group.name}' now inherits from '${targetGroup.name}'.</green>"))
                    }
                    setupItems() // Re-render in place
                }, null, 0L)
            }
        }
    }

    override fun dragEvent(event: InventoryDragEvent) {
        if (event.rawSlots.any { it < inv.size }) {
            event.isCancelled = true
        }
    }
}

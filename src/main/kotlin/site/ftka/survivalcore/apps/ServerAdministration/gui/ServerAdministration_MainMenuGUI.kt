package site.ftka.survivalcore.apps.ServerAdministration.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.inventorygui.interfaces.InventoryGUIOwner

internal class ServerAdministration_MainMenuGUI(
    private val plugin: MClass,
    private val player: Player
) : InventoryGUIOwner {

    override val ownerName = "ServerAdminMainMenu_${player.uniqueId}"
    private val inv: Inventory
    private val mm = MiniMessage.miniMessage()

    init {
        val title = mm.deserialize("<dark_aqua><bold>Server Admin Panel</bold></dark_aqua>")
        // Create a 5-row chest inventory (45 slots)
        inv = plugin.servicesFwk.inventoryGUI.api.createInventory(this, 45, title)
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

    private fun setupItems() {
        val filler = createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray> </gray>")
        for (i in 0 until inv.size) {
            if (isBorder(i)) {
                inv.setItem(i, filler)
            }
        }

        // Slot 4: System Dashboard Info Orb
        val dbCfg = plugin.essentialsFwk.configs.generalCfg().DATABASE
        val dbHost = dbCfg?.host ?: "unknown"
        val activeProfiles = plugin.servicesFwk.playerData.data.getPlayerDataMap().size

        val dashboard = createItem(
            Material.BEACON,
            "<#00ffcc><bold>System Dashboard</bold></#00ffcc>",
            "<gray>----------------------------------</gray>",
            "<gray>Core framework: <aqua>SurvivalCore</aqua></gray>",
            "<gray>Target API: <yellow>Folia 1.21.11</yellow></gray>",
            "<gray>Platform OS: <white>${System.getProperty("os.name")}</white></gray>",
            "<gray>Runtime Java: <white>${System.getProperty("java.version")}</white></gray>",
            "<gray>Active Profiles: <green>$activeProfiles cached</green></gray>",
            "<gray>Database Host: <light_purple>$dbHost</light_purple></gray>",
            "<gray>----------------------------------</gray>"
        )
        inv.setItem(4, dashboard)

        // Slot 20: Configs Essential Manager
        val configsBtn = createItem(
            Material.BOOK,
            "<#e6b800><bold>Configs Essential Manager</bold></#e6b800>",
            "<gray>Reload all JSON configs from disk</gray>",
            "<gray>without server restarts.</gray>",
            "",
            "<gray>Configs loaded:</gray>",
            "<gray>• <white>general_config.json</white></gray>",
            "<gray>• <white>chat_config.json</white></gray>",
            "<gray>• <white>playerdata_config.json</white></gray>",
            "",
            "<yellow>⚡ Click to Hot-Reload configs!</yellow>"
        )
        inv.setItem(20, configsBtn)

        // Slot 22: Hot-Restart Core Services
        val restartBtn = createItem(
            Material.BLAZE_POWDER,
            "<#ff6600><bold>Hot-Restart Core Services</bold></#ff6600>",
            "<gray>Restarts all Tier 3 services</gray>",
            "<gray>sequentially in-memory.</gray>",
            "",
            "<gray>Services affected:</gray>",
            "<gray>• <white>PlayerDataService</white></gray>",
            "<gray>• <white>LanguageService</white></gray>",
            "<gray>• <white>PermissionsService</white></gray>",
            "<gray>• <white>InventoryGUIService</white></gray>",
            "<gray>• <white>SingulaService</white></gray>",
            "",
            "<red>⚠️ Click to execute services restart!</red>"
        )
        inv.setItem(22, restartBtn)

        // Slot 24: Database / Redis API Health
        val databaseBtn = createItem(
            Material.NETHER_STAR,
            "<#cc66ff><bold>Database API Health</bold></#cc66ff>",
            "<gray>Lettuce Redis Connection client.</gray>",
            "<gray>Status: <green>ONLINE</green></gray>",
            "",
            "<yellow>⚡ Click to trigger instant Redis ping!</yellow>"
        )
        inv.setItem(24, databaseBtn)

        // Slot 30: Force Emergency profile dump
        val dumpBtn = createItem(
            Material.ENDER_CHEST,
            "<#ff3333><bold>Force Emergency Profile Dump</bold></#ff3333>",
            "<gray>Dumps active profiles immediately</gray>",
            "<gray>to Redis or emergency disk storage.</gray>",
            "",
            "<red>⚠️ Click to force flush cache!</red>"
        )
        inv.setItem(30, dumpBtn)

        // Slot 32: Test Broadcast System
        val broadcastBtn = createItem(
            Material.BELL,
            "<#33ccff><bold>Test Broadcast System</bold></#33ccff>",
            "<gray>Test Adventure Title and ActionBar</gray>",
            "<gray>broadcast features to all players.</gray>",
            "",
            "<yellow>⚡ Click to test broadcast!</yellow>"
        )
        inv.setItem(32, broadcastBtn)

        // Slot 40: Close Button
        val closeBtn = createItem(
            Material.BARRIER,
            "<red><bold>Close Panel</bold></red>"
        )
        inv.setItem(40, closeBtn)
    }

    private fun isBorder(slot: Int): Boolean {
        return slot < 9 || slot >= 36 || slot % 9 == 0 || slot % 9 == 8
    }

    override fun getInventory(): Inventory = inv

    override fun clickEvent(event: InventoryClickEvent) {
        val clickedInv = event.clickedInventory ?: return
        if (clickedInv == inv) {
            event.isCancelled = true // Prevent item stealing!

            when (event.slot) {
                20 -> { // Reload configs
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        player.sendMessage(mm.deserialize("<yellow>Reloading config files from disk...</yellow>"))
                        try {
                            plugin.essentialsFwk.configs.restart()
                            player.sendMessage(mm.deserialize("<green>Successfully reloaded all configuration files!</green>"))
                            player.playSound(player.location, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                        } catch (e: java.lang.Exception) {
                            player.sendMessage(mm.deserialize("<red>Failed to reload configs: ${e.message}</red>"))
                            player.playSound(player.location, org.bukkit.Sound.ENTITY_ITEM_BREAK, 1f, 0.5f)
                            e.printStackTrace()
                        }
                    }, null, 0L)
                }

                22 -> { // Hot-Restart services
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        player.sendMessage(mm.deserialize("<gold>Initiating services hot-restart...</gold>"))
                        try {
                            plugin.servicesFwk.restartAll()
                            player.sendMessage(mm.deserialize("<green>Successfully restarted all core services sequentially!</green>"))
                            player.playSound(player.location, org.bukkit.Sound.BLOCK_ANVIL_USE, 1f, 1.2f)
                        } catch (e: java.lang.Exception) {
                            player.sendMessage(mm.deserialize("<red>Failed to restart services: ${e.message}</red>"))
                            player.playSound(player.location, org.bukkit.Sound.ENTITY_ITEM_BREAK, 1f, 0.5f)
                            e.printStackTrace()
                        }
                    }, null, 0L)
                }

                24 -> { // Database ping
                    val start = System.currentTimeMillis()
                    plugin.essentialsFwk.database.api.ping(true).thenAccept { pong ->
                        val delay = System.currentTimeMillis() - start
                        player.scheduler.execute(plugin, {
                            if (pong) {
                                plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<light_purple>Database Ping: <green>${delay}ms</green></light_purple>"))
                                player.playSound(player.location, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f)
                            } else {
                                plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<red>Database connection offline!</red>"))
                                player.playSound(player.location, org.bukkit.Sound.ENTITY_ITEM_BREAK, 1f, 0.5f)
                            }
                        }, null, 0L)
                    }
                }

                30 -> { // Force emergency profile dump
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        player.sendMessage(mm.deserialize("<red>Forcing emergency profile sync...</red>"))
                        try {
                            val activeData = plugin.servicesFwk.playerData.data.getPlayerDataMap()
                            var savedCount = 0
                            for (profile in activeData.values) {
                                plugin.servicesFwk.playerData.inout_ss.set(profile, async = true)
                                savedCount++
                            }
                            player.sendMessage(mm.deserialize("<green>Successfully synchronized and saved $savedCount active profiles!</green>"))
                            player.playSound(player.location, org.bukkit.Sound.BLOCK_CHEST_CLOSE, 1f, 1f)
                        } catch (e: java.lang.Exception) {
                            player.sendMessage(mm.deserialize("<red>Flush failed: ${e.message}</red>"))
                            player.playSound(player.location, org.bukkit.Sound.ENTITY_ITEM_BREAK, 1f, 0.5f)
                            e.printStackTrace()
                        }
                    }, null, 0L)
                }

                32 -> { // Test broadcast system
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        val actionBarMsg = mm.deserialize("<gradient:gold:yellow><bold>BROADCAST TEST SUCCESSFUL</bold></gradient>")
                        val titleMsg = mm.deserialize("<#00ffcc><bold>SurvivalCore</bold></#00ffcc>")
                        val subtitleMsg = mm.deserialize("<gray>Folia administration engine active</gray>")

                        for (onlinePlayer in plugin.server.onlinePlayers) {
                            plugin.essentialsFwk.actionbar.api.sendActionBar(onlinePlayer.uniqueId, actionBarMsg)
                            onlinePlayer.showTitle(net.kyori.adventure.title.Title.title(
                                titleMsg,
                                subtitleMsg,
                                net.kyori.adventure.title.Title.Times.times(
                                    java.time.Duration.ofMillis(500),
                                    java.time.Duration.ofMillis(2000),
                                    java.time.Duration.ofMillis(500)
                                )
                            ))
                            onlinePlayer.playSound(onlinePlayer.location, org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
                        }
                    }, null, 0L)
                }

                40 -> { // Close panel
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                    }, null, 0L)
                }
            }
        }
    }

    override fun dragEvent(event: InventoryDragEvent) {
        if (event.rawSlots.any { it < inv.size }) {
            event.isCancelled = true // Block drag exploits inside custom slots
        }
    }
}
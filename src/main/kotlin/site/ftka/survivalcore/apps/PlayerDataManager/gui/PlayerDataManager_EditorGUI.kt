package site.ftka.survivalcore.apps.PlayerDataManager.gui

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.chat.objects.ChatScreen
import site.ftka.survivalcore.essentials.chat.objects.ChatScreenPage
import site.ftka.survivalcore.services.inventorygui.interfaces.InventoryGUIOwner
import java.util.UUID
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class PlayerDataManager_EditorGUI(
    private val plugin: MClass,
    private val player: Player,
    private val targetUUID: UUID,
    private val currentPath: List<String> = emptyList()
) : InventoryGUIOwner {

    override val ownerName = "PlayerDataManager_Edit_${player.uniqueId}"
    private val inv: Inventory
    private val mm = MiniMessage.miniMessage()
    private val gson = Gson()
    
    // Slot mapping to JSON keys
    private val slotKeyMap = mutableMapOf<Int, String>()
    private val slotIsObjectMap = mutableMapOf<Int, Boolean>()

    init {
        val pathStr = if (currentPath.isEmpty()) "Root" else currentPath.joinToString(".")
        val title = mm.deserialize("<#00ffcc><bold>Editing: $pathStr</bold></#00ffcc>")
        inv = plugin.servicesFwk.inventoryGUI.api.createInventory(this, 54, title)
        
        loadAndDisplay()
    }

    private fun loadAndDisplay() {
        plugin.servicesFwk.playerData.inout_ss.get(targetUUID, async = true).thenAccept { pdata ->
            player.scheduler.execute(plugin, {
                if (pdata != null) {
                    val rootJson = gson.toJsonTree(pdata).asJsonObject
                    var currentNode: JsonElement = rootJson
                    
                    for (step in currentPath) {
                        if (currentNode.isJsonObject && currentNode.asJsonObject.has(step)) {
                            currentNode = currentNode.asJsonObject.get(step)
                        }
                    }

                    if (currentNode.isJsonObject) {
                        setupItems(currentNode.asJsonObject)
                    } else {
                        inv.setItem(13, createItem(Material.BARRIER, "<red>Invalid Path</red>"))
                    }
                } else {
                    inv.setItem(13, createItem(Material.BARRIER, "<red>Failed to load PlayerData</red>"))
                }
            }, null, 0L)
        }
    }

    private fun createItem(material: Material, name: String, vararg lore: String): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item
        meta.displayName(mm.deserialize(name))
        meta.lore(lore.map { mm.deserialize(it) })
        item.itemMeta = meta
        return item
    }

    private fun setupItems(node: JsonObject) {
        inv.clear()
        slotKeyMap.clear()
        slotIsObjectMap.clear()

        var slot = 0
        for ((key, element) in node.entrySet()) {
            if (slot >= 45) break

            if (element.isJsonObject) {
                val item = createItem(
                    Material.CHEST,
                    "<#ffff66><bold>$key</bold></#ffff66>",
                    "<gray>Type: Object</gray>",
                    "<yellow>Click to expand</yellow>"
                )
                inv.setItem(slot, item)
                slotKeyMap[slot] = key
                slotIsObjectMap[slot] = true
            } else if (element.isJsonPrimitive) {
                val primitive = element.asJsonPrimitive
                val valueStr = primitive.asString
                val typeStr = if (primitive.isBoolean) "Boolean" else if (primitive.isNumber) "Number" else "String"
                
                val item = createItem(
                    Material.PAPER,
                    "<#99ff99><bold>$key</bold></#99ff99>",
                    "<gray>Type: $typeStr</gray>",
                    "<gray>Value: <white>$valueStr</white></gray>",
                    "",
                    "<yellow>Click to edit value in chat</yellow>"
                )
                inv.setItem(slot, item)
                slotKeyMap[slot] = key
                slotIsObjectMap[slot] = false
            } else if (element.isJsonArray) {
                val item = createItem(
                    Material.MINECART,
                    "<#ff99cc><bold>$key</bold></#ff99cc>",
                    "<gray>Type: Array</gray>",
                    "<red>Editing arrays is not supported yet</red>"
                )
                inv.setItem(slot, item)
            }
            
            slot++
        }

        // Back Button
        val backBtn = createItem(Material.ARROW, "<red>Back</red>")
        inv.setItem(45, backBtn)
    }

    override fun getInventory(): Inventory = inv

    override fun clickEvent(event: InventoryClickEvent) {
        val clickedInv = event.clickedInventory ?: return
        if (clickedInv == inv) {
            event.isCancelled = true

            if (event.slot == 45) {
                player.scheduler.execute(plugin, {
                    player.closeInventory()
                    if (currentPath.isEmpty()) {
                        val gui = PlayerDataManager_MainGUI(plugin, player, targetUUID)
                        player.openInventory(gui.inventory)
                    } else {
                        val parentPath = currentPath.dropLast(1)
                        val gui = PlayerDataManager_EditorGUI(plugin, player, targetUUID, parentPath)
                        player.openInventory(gui.inventory)
                    }
                }, null, 0L)
                return
            }

            if (slotKeyMap.containsKey(event.slot)) {
                val key = slotKeyMap[event.slot]!!
                val isObject = slotIsObjectMap[event.slot]!!

                if (isObject) {
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        val nextPath = currentPath + key
                        val gui = PlayerDataManager_EditorGUI(plugin, player, targetUUID, nextPath)
                        player.openInventory(gui.inventory)
                    }, null, 0L)
                } else {
                    player.scheduler.execute(plugin, {
                        player.closeInventory()
                        startEditChatScreen(key)
                    }, null, 0L)
                }
            }
        }
    }

    private fun startEditChatScreen(keyToEdit: String) {
        val fullPathStr = (currentPath + keyToEdit).joinToString(".")
        
        val editScreen = object : ChatScreen() {
            override val name = "PlayerDataManager_Edit"
            override var screenContent = mutableMapOf(
                "home" to ChatScreenPage(
                    message = "\n<#99ff99><bold>Editing Field:</bold> $fullPathStr</#99ff99>\n<gray>Please type the new value in chat.</gray>\n<gray>Type</gray> <red>/exitscreen</red> <gray>to cancel.</gray>\n",
                    process = { mm.deserialize(it) },
                    onChat = { input, sender ->
                        plugin.essentialsFwk.chat.api.stopScreen(sender.uniqueId, name)
                        
                        sender.sendMessage(mm.deserialize("<yellow>Applying modification...</yellow>"))
                        
                        GlobalScope.launch {
                            val result = plugin.servicesFwk.playerData.inout_ss.makeModification(targetUUID) { pdata ->
                                try {
                                    val root = gson.toJsonTree(pdata).asJsonObject
                                    var current = root
                                    
                                    for (step in currentPath) {
                                        if (current.has(step)) current = current.get(step).asJsonObject
                                    }
                                    
                                    val prim: JsonPrimitive = when {
                                        input.equals("true", ignoreCase = true) -> JsonPrimitive(true)
                                        input.equals("false", ignoreCase = true) -> JsonPrimitive(false)
                                        input.toDoubleOrNull() != null -> {
                                            val d = input.toDouble()
                                            if (d % 1 == 0.0) JsonPrimitive(d.toLong()) else JsonPrimitive(d)
                                        }
                                        else -> JsonPrimitive(input)
                                    }
                                    
                                    current.add(keyToEdit, prim)
                                    
                                    val newPdata = plugin.servicesFwk.playerData.fromJson(root.toString())
                                    if (newPdata != null) {
                                        pdata.information = newPdata.information
                                        pdata.state = newPdata.state
                                        pdata.permissions = newPdata.permissions
                                        pdata.settings = newPdata.settings
                                        pdata.unlockedChunks = newPdata.unlockedChunks
                                        pdata.updateTimestamp = System.currentTimeMillis()
                                        true
                                    } else false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    false
                                }
                            }
                            
                            sender.scheduler.execute(plugin, {
                                if (result == site.ftka.survivalcore.services.playerdata.subservices.PlayerData_InputOutputSubservice.PlayerDataModificationResult.SUCCESS) {
                                    sender.sendMessage(mm.deserialize("<green>Successfully updated $fullPathStr!</green>"))
                                } else {
                                    sender.sendMessage(mm.deserialize("<red>Failed to update field: $result</red>"))
                                }
                                
                                val gui = PlayerDataManager_EditorGUI(plugin, sender, targetUUID, currentPath)
                                sender.openInventory(gui.inventory)
                            }, null, 0L)
                        }
                    }
                )
            )
        }
        
        plugin.essentialsFwk.chat.api.showScreen(player.uniqueId, editScreen)
    }

    override fun dragEvent(event: InventoryDragEvent) {
        if (event.rawSlots.any { it < inv.size }) {
            event.isCancelled = true
        }
    }
}

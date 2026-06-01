package site.ftka.survivalcore.apps.PermissionsManager.gui

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import site.ftka.survivalcore.MClass
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PermissionsManager_ChatInputInterceptor(private val plugin: MClass) : Listener {
    private val mm = MiniMessage.miniMessage()

    class InterceptAction(
        val prompt: String,
        val callback: (String) -> Unit
    )

    private val activeInterceptors = ConcurrentHashMap<UUID, InterceptAction>()

    fun startIntercept(uuid: UUID, prompt: String, callback: (String) -> Unit) {
        val player = plugin.server.getPlayer(uuid) ?: return
        activeInterceptors[uuid] = InterceptAction(prompt, callback)

        // Visually clear screen for clean dialogue look
        plugin.essentialsFwk.chat.api.clearChat(uuid)

        player.sendMessage(mm.deserialize("<gray>┌──────────────────────────────────────────────────┐</gray>"))
        player.sendMessage(mm.deserialize("<gray>│</gray> <gold><b>★ INPUT REQUESTED ★</b></gold>"))
        player.sendMessage(mm.deserialize("<gray>│</gray> <white>$prompt</white>"))
        player.sendMessage(mm.deserialize("<gray>│</gray>"))
        player.sendMessage(mm.deserialize("<gray>│</gray> <gray>Type your response in chat.</gray>"))
        player.sendMessage(mm.deserialize("<gray>│</gray> <gray>Type <red>cancel</red> to abort this operation.</gray>"))
        player.sendMessage(mm.deserialize("<gray>└──────────────────────────────────────────────────┘</gray>"))
    }

    fun isIntercepting(uuid: UUID): Boolean {
        return activeInterceptors.containsKey(uuid)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChatLowest(event: AsyncChatEvent) {
        val player = event.player
        val action = activeInterceptors[player.uniqueId] ?: return

        // Intercept and cancel the chat message
        event.isCancelled = true

        val plainText = PlainTextComponentSerializer.plainText().serialize(event.message()).trim()

        // Clean prompt traces and restore chat history
        plugin.essentialsFwk.chat.api.clearChat(player.uniqueId)
        plugin.essentialsFwk.chat.api.restorePlayerChat(player.uniqueId, 50)

        if (plainText.equals("cancel", ignoreCase = true)) {
            plugin.essentialsFwk.actionbar.api.sendActionBar(player.uniqueId, mm.deserialize("<red>✖ Operation cancelled.</red>"))
            return
        }

        // Run the callback
        action.callback(plainText)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onChatMonitor(event: AsyncChatEvent) {
        activeInterceptors.remove(event.player.uniqueId)
    }
}

package site.ftka.survivalcore.apps.ChatManager.screens

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.chat.objects.ChatScreen
import site.ftka.survivalcore.essentials.chat.objects.ChatScreenPage
import java.util.*


internal class ChatManager_ChatScreen(private val plugin: MClass, private var targetUUID: UUID? = null): ChatScreen() {

    private val svc = plugin.essentialsFwk.chat

    /*
        ChatScreen data
     */

    override val name = "ChatManager"
    override var screenContent: MutableMap<String, ChatScreenPage> = mutableMapOf()

    /*
        lang (should be initialized before init)
     */

    /**
     * placeholders:
     * @see {lastLine} -> last line of the screen, to be defined
     */
    private val home_panel = buildString {
        appendLine("                 <gold><bold>Chat Manager</bold></gold>")
        appendLine("<color:#a0ac3a>")
        appendLine("  Type the name of the player")
        appendLine("  you want to control")
        appendLine("<reset>")
        appendLine("{lastLine}")
    }

    /**
     * placeholders:
     * @see {playername} -> player's name
     */
    private val playerPanel = buildString {
        appendLine("${svc.BACK_BUTTON_SCREEN_STANDARD}              <gold><bold>Chat Manager<reset>")
        appendLine()
        appendLine("  <color:#ff9d8c>{playername}:</color>")
        appendLine("      <gray>Manage <click:run_command:'/chat {playername} channels'><u>channels</u></click> or <click:run_command:'/chat {playername} screen'><u>active screen</u></click>")
        appendLine("<reset>")
        appendLine(svc.EXIT_SCREEN_LINE_STANDARD)
    }

    /**
     * placeholders:
     * @see {playerName} -> player's name
     * @see {activeChannels} -> active channels list (player_panel_activeChannelElement list)
     * @see {inactiveChannels} -> inactive channels list (player_panel_inactiveChannelElement list)
     */
    private val playerChannelsPanel = buildString {
        appendLine("${svc.BACK_BUTTON_SCREEN_STANDARD}               <gold><bold>Chat Manager</bold></gold>")
        appendLine("<color:#ff9d8c>  {playerName}:</color>")
        appendLine("<gray>  | List of active channels:</gray>")
        appendLine("<hover:show_text:'<gray>This is a mandatory channel!</gray>'><gray>[o]</gray></hover><dark_green>  - Personal</dark_green> <yellow>(mandatory)</yellow>")
        appendLine("{activeChannels}")
        appendLine()
        appendLine("  <gold>To enable a channel use <yellow>/s")
        appendLine("  <gold>e.g. <yellow>/s \$global")
        appendLine("<reset>")
        appendLine(svc.EXIT_SCREEN_LINE_STANDARD)
    }
    private val playerChannelsPanel_activeChannelElement = "<red><hover:show_text:'<red>Disable'><click:run_command:'/chat {playerName} channels disable {channelName}'>[-]</click></hover><dark_green>  - {channelName}"

    /**
     * placeholders:
     * @see {playerName} -> player's name
     * @see {screenLine} -> where noActiveScreen or activeScreen texts will go
     */
    private val playerScreenPanel = buildString {
        appendLine("${svc.BACK_BUTTON_SCREEN_STANDARD}               <gold><bold>Chat Manager</bold></gold>")
        appendLine()
        appendLine("<color:#ff9d8c>  {playerName}:</color>")
        appendLine("{screenLine}")
        appendLine()
        appendLine(svc.EXIT_SCREEN_LINE_STANDARD)
    }
    private val playerScreenPanel_noActiveScreen = "    <gray>Doesn't have an active screen"
    /**
     * placeholders:
     * @see {playerName} -> player's name
     * @see {activeScreenFrame} -> active screen frame
     * @see {activeScreen} -> active screen name
     */
    private val playerScreenPanel_activeScreen = "    <gray>Active screen: <green><hover:show_text:'{activeScreenFrame}<reset>'>{activeScreen}</hover> <red><click:run_command:'/chat {playerName} screen stop'><hover:show_text:'<red>Disable screen'>[x]</hover></click>"


    init {
        screenContent["home"] = homePanelPage()
        targetUUID?.let { playerSelection(targetUUID!!) }
    }

    /**
     * Select a player to target
     * @param uuid The UUID of the player to target
     */
    fun playerSelection(uuid: UUID) {
        if (targetUUID == uuid)
            return

        screenContent["player"] = playerPage(uuid)
        screenContent["player_channels"] = playerChannelsPage(uuid)
        screenContent["player_screen"] = playerScreenPage(uuid)
        targetUUID = uuid
    }

    /*
        pages
     */

    var homePanel_chatMessage: String? = null
    var homePanel_isOnline: Boolean? = null
    var homePanel_errorMsgTimeout: Long? = null

    private fun homePanelPage() = ChatScreenPage(home_panel,
            { // process
                val text = it.replace("{lastLine}", buildString {
                    var notification = false
                    homePanel_chatMessage?.let { chatMsg ->
                        homePanel_isOnline?.let { isOnline ->
                            println(isOnline)
                            if (!isOnline && homePanel_errorMsgTimeout != null) {
                                append(" <gold>{playerName} <red>is not online!".replace("{playerName}", chatMsg))
                                notification = true
                            }
                        }

                        homePanel_errorMsgTimeout?.let { timeout ->
                            if (timeout > 0L)
                                homePanel_errorMsgTimeout = timeout - 1
                            else {
                                homePanel_chatMessage = null
                                homePanel_isOnline = null
                                homePanel_errorMsgTimeout = null
                            }
                        }
                    }

                    if (!notification)
                        append(svc.EXIT_SCREEN_LINE_STANDARD)

                    homePanel_chatMessage ?: { notification = false }
                })

                return@ChatScreenPage MiniMessage.miniMessage().deserialize(text)
            },

        // onChat
            { msg, _ ->
                val targetPlayer = plugin.server.getPlayer(msg)
                if (targetPlayer == null) {
                    homePanel_chatMessage = msg
                    homePanel_isOnline = false
                    homePanel_errorMsgTimeout = 3 // 3 sec error message
                    return@ChatScreenPage
                } else {
                    playerSelection(targetPlayer.uniqueId)
                    svc.api.refreshScreen(targetPlayer.uniqueId, name, "player")
                }
            }
    )



    private fun playerPage(uuid: UUID)
        = ChatScreenPage(playerPanel,
            {
                targetUUID ?: return@ChatScreenPage MiniMessage.miniMessage().deserialize("<red>error\n${svc.EXIT_SCREEN_LINE_STANDARD}")
                val text = playerPanel
                    .replace("{playername}", plugin.essentialsFwk.usernameTracker.getName(targetUUID!!) ?: "{unknown}")
                return@ChatScreenPage MiniMessage.miniMessage().deserialize(text)
            })

    private fun playerChannelsPage(uuid: UUID)
        = ChatScreenPage(playerChannelsPanel,
            {
                targetUUID ?: return@ChatScreenPage MiniMessage.miniMessage().deserialize("<red>error\n${svc.EXIT_SCREEN_LINE_STANDARD}")

                val text = it
                    .replace("{playerName}", plugin.essentialsFwk.usernameTracker.getName(targetUUID!!) ?: "{unknown}")

                    .replace("{activeChannels}", buildString {
                        for (channel in svc.api.getPlayerActiveChannels(targetUUID!!))
                            appendLine(playerChannelsPanel_activeChannelElement.replace("{channelName}", channel))
                    }

                    .replace("{playerName}", plugin.essentialsFwk.usernameTracker.getName(targetUUID!!) ?: "{unknown}"))
                return@ChatScreenPage MiniMessage.miniMessage().deserialize(text)
            },

            // onChat
            {msg, _ ->},

            // S command: activate channel
            { args, _ ->
                if (args.size == 1 && targetUUID != null)
                    svc.api.addActiveChannel(targetUUID!!, args[0])
            },

            { args, _ ->
                return@ChatScreenPage svc.api.getAllChannels(true, targetUUID).toList()
            }
        )

    private fun playerScreenPage(uuid: UUID)
        = ChatScreenPage(playerScreenPanel,
            {
                targetUUID ?: return@ChatScreenPage MiniMessage.miniMessage().deserialize("<red>error\n${svc.EXIT_SCREEN_LINE_STANDARD}")
                val targetName = plugin.essentialsFwk.usernameTracker.getName(targetUUID!!) ?: "{unknown}"

                var text = it.replace("{playerName}", targetName)

                    if (svc.api.isPlayerInsideScreen(targetUUID!!)) {
                        val antiHoverMiniMessage = MiniMessage.builder()
                            .tags(
                                TagResolver.builder()
                                    .resolver(StandardTags.color())
                                    .resolver(StandardTags.decorations())
                                    .build()
                            ).build()

                        val activeScreen = svc.api.getActiveScreen(targetUUID!!)
                        val processIt = activeScreen?.name != name // Do NOT process the active screen if it's the same as this screen
                        val activeScreenFrame_serialized = antiHoverMiniMessage.serialize(activeScreen?.getFrame(processIt) ?: Component.text("unknown"))

                        text = text.replace("{screenLine}", buildString {
                            append(playerScreenPanel_activeScreen
                                .replace("{playerName}", targetName)
                                .replace("{activeScreenFrame}", activeScreenFrame_serialized)
                                .replace("{activeScreen}", activeScreen?.name ?: "{unknown}"))
                        })
                    } else
                        text = text.replace("{screenLine}", playerScreenPanel_noActiveScreen)

                return@ChatScreenPage MiniMessage.miniMessage().deserialize(text)
            }
        )
}
package site.ftka.survivalcore.apps.PermissionsManager.screens

import net.kyori.adventure.text.minimessage.MiniMessage
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.chat.objects.ChatScreen
import site.ftka.survivalcore.essentials.chat.objects.ChatScreenPage
import java.util.UUID

internal class PermissionsManager_ChatScreen(
    private val plugin: MClass,
    private var targettingGroup: Boolean? = null,
    private var targetUUID: UUID? = null
    ): ChatScreen() {

    private val chat = plugin.essentialsFwk.chat
    private val permsAPI = plugin.servicesFwk.permissions.api

    override val name = "PermissionsManager"

    /*
        lang
     */

    val homePanel = buildString {
        appendLine("<gold><b>          Permissions Manager</b></gold>")
        appendLine("<color:#a0ac3a>")
        appendLine("  To select a player, type it's name")
        appendLine("  to control a group, do <gold><hover:show_text:'<gray><i>e.g. <gold>/s Housemaster'>/s group</hover>")
        appendLine("<reset>")
        appendLine("{lastLine}")
    }

    /**
     * | placeholders:
     * @see {groupList} -> list of all groups the player's on, each group as a groupPanel_groupElement object.
     */
    val groupPanel = buildString {
        appendLine("<gold><b>          Permissions Manager Panel</b></gold>")
        appendLine("<gray>                 <green>Group<gray>     |     <click:run_command:'/permissions player'>Player</click>")
        appendLine("{groupList}")
        appendLine("<red><click:run_command:'/exitscreen'>exit</click>")
    }

    /**
     * | placeholders:
     * @see {prefix} -> group's prefix
     * @see {primaryColor} -> group's primary color
     * @see {name} -> group's name
     */
    val groupPanel_groupElement = "  ->  <click:run_command:'/permissions group {name}'>{prefix} <{primaryColor}>{name}</click>"

    /**
     * | placeholders:
     * @see {name} -> group's name
     * @see {prefix} -> group's prefix
     * @see {primaryColor} -> group's primary color
     * @see {permissionsList} -> list of all permissions the group has, each permission as a group_panel_specificGroup_permissionElement object.
     */
    var group_panel_specificGroup = buildString {
        appendLine("<gold><b>          Permissions Manager Panel</b></gold>")
        appendLine("  ->  <{primaryColor}>{name}")
        appendLine("{permissionsList}")
        appendLine("<red><click:run_command:'/exitscreen'>exit</click>")
    }

    /**
     * | placeholders:
     * @see {permission} -> permission's name
     * @see {name} -> group's name
     */
    val group_panel_specificGroup_permissionElement = buildString {
        appendLine("  <yellow>{permission} ")
        appendLine("<red> <click:run_command:'/groups {name} removeperm {permission}'>")
        appendLine("<hover:show_text:'<red>remove'><b>[-]</b></hover></click>")
    }

    init {
        screenContent["home"] = homePage()
        screenContent["player_groups"] = playerGroupPage()
        screenContent["groups_home"] = playerGroupPage()
        screenContent["group_perms"] = groupPermissionsPage()
        screenContent["group_permissions"] = groupPermissionsPage()
    }

    fun initializeTarget(targettingGroup: Boolean, targetUUID: UUID) {
        this.targettingGroup = targettingGroup
        this.targetUUID = targetUUID
    }

    /*
        pages
     */

    // could be a group or a player's name
    private var homePanel_notFoundThing: String = "unknown"
    private var homePanel_notFoundThing_notifTimeout = 0

    fun homePage(): ChatScreenPage
        = ChatScreenPage(
            homePanel,
            { // process
                val text = it.replace("{lastLine}",
                    if (homePanel_notFoundThing_notifTimeout > 0) {
                        homePanel_notFoundThing_notifTimeout--
                        " <gold>${homePanel_notFoundThing} <red>does not exist."
                    }
                    else
                        chat.EXIT_SCREEN_LINE_STANDARD
                )
                return@ChatScreenPage MiniMessage.miniMessage().deserialize(text)
            },

        // onChat: selecting player
            { msg, sender ->
                // does uuid exist?
                val playerUUID =
                    plugin.server.getPlayer(msg)?.uniqueId
                        ?: plugin.essentialsFwk.usernameTracker.getUUID(msg)

                // player not found, display notification
                if (playerUUID == null) {
                    homePanel_notFoundThing = msg
                    homePanel_notFoundThing_notifTimeout = 3
                    return@ChatScreenPage
                }

                // player found, initialize and display player's page
                initializeTarget(false, playerUUID)
                chat.api.refreshScreen(sender.uniqueId, name, "player_groups") // default is groups. can switch to perms
            },

        // onSCommand: selecting group
            { args, sender ->
                if (args.isEmpty())
                    return@ChatScreenPage

                // group doesn't exist
                if (permsAPI.getGroup(args[0]) == null) {
                    homePanel_notFoundThing = args[0]
                    homePanel_notFoundThing_notifTimeout = 3
                    return@ChatScreenPage
                }

                val group = permsAPI.getGroup(args[0])!!
                initializeTarget(true, group.uuid)
                chat.api.refreshScreen(sender.uniqueId, name, "group_permissions") // default is perms. can switch to inheritances
            },

        // onSTabComplete: suggest groups
            { completions, _ ->
                val groups = plugin.servicesFwk.permissions.data.getGroups()
                return@ChatScreenPage groups.map { it.name }
            }
        )

    fun playerGroupPage(): ChatScreenPage {
        return ChatScreenPage(
            groupPanel,
            {
                val groupList = StringBuilder()
                for (group in plugin.servicesFwk.permissions.data.getGroups()) {
                    groupList.append(groupPanel_groupElement
                        .replace("{prefix}", group.tag)
                        .replace("{primaryColor}", group.primaryColor)
                        .replace("{name}", group.name)
                    )
                    groupList.append("\n")
                }

                return@ChatScreenPage MiniMessage.miniMessage().deserialize(
                    it.replace("{groupList}", groupList.toString()))
            }
        )
    }

    fun groupPermissionsPage(): ChatScreenPage {
        return ChatScreenPage(
            group_panel_specificGroup,
            {
                val uuid = targetUUID ?: return@ChatScreenPage MiniMessage.miniMessage().deserialize("<red>No group selected.")
                val group = plugin.servicesFwk.permissions.data.getGroup(uuid) ?: return@ChatScreenPage MiniMessage.miniMessage().deserialize("<red>Group not found.")

                val permsBuilder = StringBuilder()
                for (perm in group.perms) {
                    permsBuilder.append(group_panel_specificGroup_permissionElement
                        .replace("{permission}", perm)
                        .replace("{name}", group.name)
                    )
                }

                val result = it.replace("{name}", group.name)
                    .replace("{primaryColor}", group.primaryColor)
                    .replace("{permissionsList}", permsBuilder.toString())

                return@ChatScreenPage MiniMessage.miniMessage().deserialize(result)
            }
        )
    }

}
package site.ftka.survivalcore.apps.PermissionsManager.lang

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chat.objects.ChatScreenPage

class PermissionsManager_CommandLang(private val plugin: MClass) {

    val home_panel = buildString {
        appendLine("<gold><b>          Permissions Manager Panel</b></gold>")
        appendLine("<gray>                 <click:run_command:'/permissions group'>Group</click>     |     <click:run_command:'/permissions player'>Player</click>")
        appendLine("<red><click:run_command:'/exitScreen'>exit</click>")
    }

    fun screenPage_home_panel(): ChatScreenPage {
        return ChatScreenPage(
            Component.text(home_panel),
            {
                return@ChatScreenPage MiniMessage.miniMessage().deserialize(home_panel)
            },

            { }

        )
    }

    /**
     | placeholders:
     | {groupList} -> groups list
     | to be replaced with group_panel_groupElement for each group
     */
    // Vamos a cambiar la forma de group_panel a buildString
    val group_panel = buildString {
        appendLine("<gold><b>          Permissions Manager Panel</b></gold>")
        appendLine("<gray>                 <green>Group<gray>     |     <click:run_command:'/permissions player'>Player</click>")
        appendLine("{groupList}")
        appendLine("<red><click:run_command:'/exitPanel'>exit</click>")
    }

    /**
    placeholders:
    | {prefix} -> group's prefix
    | {primaryColor} -> group's primary color
    | {name} -> group's name
     */
    val group_panel_groupElement = "  ->  <click:run_command:'/permissions group {name}'>{prefix} <{primaryColor}>{name}</click>"

    fun screenPage_group_panel(): ChatScreenPage {
        return ChatScreenPage(
            Component.text(group_panel),
            {
                /*
                    {groupList} has to be replaced with a list of all groups.
                    Each group must use the group_panel_groupElement template.
                 */

                val groupList = StringBuilder()
                for (group in plugin.servicesFwk.permissions.data.getGroups()) {
                    groupList.append(group_panel_groupElement
                        .replace("{prefix}", group.tag)
                        .replace("{primaryColor}", group.primaryColor)
                        .replace("{name}", group.name)
                    )
                    groupList.append("\n")
                }

                return@ChatScreenPage MiniMessage.miniMessage().deserialize(
                    group_panel.replace("{groupList}", groupList.toString()))
            },

            { }

        )
    }

    /**
     | placeholders:
     | {prefix} -> group's prefix
     | {primaryColor} -> group's primary color
     | {name} -> group's name
     | {permissionsList} -> {permissionElement} list
     */
    var group_panel_specificGroup = buildString {
        appendLine("<gold><b>          Permissions Manager Panel</b></gold>")
        appendLine("  ->  <click:run_command:'/permissions group {name}'>{prefix} <{primaryColor}>{name}</click>")
        appendLine("{prefix} <{primaryColor}>{name}")
        appendLine("{permissionsList}")
        appendLine("<click:run_command:'/permissions group {name} add'><green>+ New permission</click>")
        appendLine("<red><click:run_command:'/exitPanel'>exit</click>")
    }

    /**
     | placeholders:
     | {permission} -> permission
     | {name} -> group's name
     */
    val group_panel_specificGroup_permissionElement = buildString {
        appendLine("  <yellow>{permission} ")
        appendLine("<red> <click:run_command:'/permissions group {name} remove {permission}'>")
        appendLine("<hover:show_text:'<red>remove'><b>[-]</b></hover></click>")
    }

}
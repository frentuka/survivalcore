package site.ftka.survivalcore.apps.PermissionsManager.lang

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chat.objects.ChatScreenPage

class PermissionsManager_CommandLang(private val plugin: MClass) {

    val home_panel = "<gold><b>          Permissions Manager Panel</b></gold>\n" +
                "<gray>                 <click:run_command:'/permissions group'>Group</click>     |     <click:run_command:'/permissions player'>Player</click>\n" +
                "\n" +
                "\n" +
                "\n" +
                "<red><click:run_command:'/exitScreen'>exit</click>"

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
    val group_panel = "<gold><b>          Permissions Manager Panel</b></gold>\n" +
            "<gray>                 <green>Group<gray>     |     <click:run_command:'/permissions player'>Player</click>\n" +
            "\n" +
            "{groupList}" +
            "\n" +
            "<red><click:run_command:'/exitPanel'>exit</click>"

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
    var group_panel_specificGroup = "<gold><b>          Permissions Manager Panel</b></gold>\n" +
            "<gray>                 <green>Group<gray>     |     <click:run_command:'/permissions player'>Player</click>\n" +
            "\n" +
            "  {prefix} <{primaryColor}>{name}\n" +
            "\n" +
            "{permissionsList}\n" +
            "<click:run_command:'/permissions group {name} add'><green>+ New permission</click>\n" +
            "\n" +
            "<red><click:run_command:'/exitPanel'>exit</click>"

    /**
     | placeholders:
     | {permission} -> permission
     | {name} -> group's name
     */
    val group_panel_specificGroup_permissionElement = "  <yellow>{permission} " +
            "<red> <click:run_command:'/permissions group {name} remove {permission}'>" +
            "<hover:show_text:'<red>remove'><b>[-]</b></hover></click>"

}
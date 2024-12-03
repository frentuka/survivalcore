package site.ftka.survivalcore.apps.PermissionsManager.lang

import net.kyori.adventure.text.minimessage.MiniMessage

class PermissionsManager_CommandLang {

    val home_panel = "<gold><b>          Permissions Manager Panel</b></gold>\n" +
                "<gray>                 <click:run_command:'/permissions group'>Group</click>     |     <click:run_command:'/permissions player'>Player</click>\n" +
                "\n" +
                "<red><click:run_command:'/exitPanel'>exit</click>"

    // placeholders:
    // {groupList} -> groups list
    // to be replaced with group_panel_groupElement for each group
    val group_panel = "<gold><b>          Permissions Manager Panel</b></gold>\n" +
            "<gray>                 <green>Group<gray>     |     <click:run_command:'/permissions player'>Player</click>\n" +
            "\n" +
            "{groupList}\n" +
            "\n" +
            "<red><click:run_command:'/exitPanel'>exit</click>"

    // placeholders:
    // {prefix} -> group's prefix
    // {primaryColor} -> group's primary color
    // {name} -> group's name
    val group_panel_groupElement = "  ->  {prefix} <{primaryColor}>{name}"

    // placeholders:
    // {prefix} -> group's prefix
    // {primaryColor} -> group's primary color
    // {name} -> group's name
    // {permissionsList} -> permissionElement list
    val group_panel_specificGroup = "<gold><b>          Permissions Manager Panel</b></gold>\n" +
            "<gray>                 <green>Group<gray>     |     <click:run_command:'/permissions player'>Player</click>\n" +
            "\n" +
            "  {prefix} <{primaryColor}>{name}\n" +
            "\n" +
            "{permissionsList}\n" +
            "<click:run_command:'/permissions group {name} add'><green>+ New permission</click>\n" +
            "\n" +
            "<red><click:run_command:'/exitPanel'>exit</click>"

    // placeholders:
    // {permission} -> permission
    // {name} -> group's name
    val group_panel_specificGroup_permissionElement = "  <yellow>{permission} " +
            "<red> <click:run_command:'/permissions group {name} remove {permission}'>" +
            "<hover:show_text:'<red>remove'><b>[-]</b></hover></click>"

}
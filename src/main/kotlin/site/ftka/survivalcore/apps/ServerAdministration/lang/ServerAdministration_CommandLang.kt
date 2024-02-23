package site.ftka.survivalcore.apps.ServerAdministration.lang

import net.kyori.adventure.text.minimessage.MiniMessage

class ServerAdministration_CommandLang {

    private val mm = MiniMessage.miniMessage()

    val help_message = mm.deserialize(
        " \n<#2cd13f><bold>Server Administration Panel</bold>\n" +
                "<#AAAA00>Usage:\n" +
                "<white>\n" +
                "/server <click:run_command:'/{cmd} app '><app</click>/<click:run_command:'/{cmd} service'>service></click> <name>\n" +
                " ")


}
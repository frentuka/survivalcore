package site.ftka.survivalcore.apps.ServerAdministration.lang

import net.kyori.adventure.text.minimessage.MiniMessage

internal class ServerAdministration_CommandLang {

    private val mm = MiniMessage.miniMessage()

    val help_message = mm.deserialize(
        buildString {
            appendLine()
            appendLine("<#2cd13f><bold>Server Administration Panel</bold>")
            appendLine("<#AAAA00>Usage:")
            appendLine("<white>")
            appendLine("/server <click:run_command:'/{cmd} app '><app</click>/<click:run_command:'/{cmd} service'>service></click> <name>")
        }
    )
}
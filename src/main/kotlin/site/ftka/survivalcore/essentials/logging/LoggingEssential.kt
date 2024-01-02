package site.ftka.survivalcore.essentials.logging

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.logging.objects.Log
import site.ftka.survivalcore.essentials.logging.objects.ServiceLogger

class LoggingEssential(private val plugin: MClass) {

    val logsFolderPath = plugin.dataFolder.absolutePath + "\\logs"

    // Won't print logs with level above default.
    private val printableLogLevel = LogLevel.DEBUG

    fun print(tag: Component, log: Log) {
        // Don't print higher-than-intended log levels.
        if (log.level > printableLogLevel) return

        plugin.server.consoleSender.sendMessage(tag.colorIfAbsent(NamedTextColor.AQUA).append(Component.text(" | ").color(NamedTextColor.DARK_GRAY).append(log.text.colorIfAbsent(NamedTextColor.YELLOW)))) // colorized
    }

    fun getLog(serviceName: String, serviceTag: Component) = ServiceLogger(this, serviceName, serviceTag)

    enum class LogLevel(value: Int) {
        DEBUG(9), HIGH(3), NORMAL(2), LOW(1)
    }
}
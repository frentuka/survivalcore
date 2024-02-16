package site.ftka.survivalcore.initless.logging

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.objects.Log
import site.ftka.survivalcore.initless.logging.objects.ServiceLogger

class LoggingInitless(private val plugin: MClass) {

    val logsFolderPath = plugin.dataFolder.absolutePath + "\\logs"

    // below vars will switch one color to another for each message
    // to improve readability
    private val messageColorSwitch = Pair(TextColor.fromHexString("#FFFF22")!!, TextColor.fromHexString("#FFFFBB")!!)
    private var currentMessageColor = true // true = first, false = second
    fun print(tag: Component, log: Log) {
        val msgColor = if (currentMessageColor) messageColorSwitch.first
        else messageColorSwitch.second
        currentMessageColor = !currentMessageColor

        plugin.server.consoleSender.sendMessage(
            tag.colorIfAbsent(NamedTextColor.AQUA).append(
                Component.text(" | ").color(NamedTextColor.DARK_GRAY)
                    .append(log.text.colorIfAbsent(msgColor)))) // colorized
    }

    fun getLog(serviceName: String, serviceTag: Component) = ServiceLogger(this, serviceName, serviceTag)

    enum class LogLevel(value: Int) {
        DEBUG(9), HIGH(3), NORMAL(2), LOW(1)
    }
}
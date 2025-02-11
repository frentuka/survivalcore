package site.ftka.survivalcore.initless.logging

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.objects.Log
import site.ftka.survivalcore.initless.logging.objects.LoggingInstance

/*
    LoggingInitless is a class that provides logging functionality
    for the plugin. It is used to log messages to the console, and
    to dump logs into files. It also provides a way to get a
    ServiceLogger object, which is used to log messages for a
    specific service.

    Should never be used externally.
 */
internal class LoggingInitless(private val plugin: MClass) {

    enum class LogLevel(value: Int) {
        DEBUG(9), HIGH(3), NORMAL(2), LOW(1)
    }

    val defaultTextColor: NamedTextColor = NamedTextColor.YELLOW;

    val logsFolderPath = plugin.dataFolder.absolutePath + "\\logs"

    // below vars will switch one color to another for each message
    // to improve readability
    private val messageColorSwitch = Pair(TextColor.fromHexString("#FFFF22")!!, TextColor.fromHexString("#FFFFBB")!!)
    private var currentMessageColor = true // true = first, false = second
    fun print(tag: Component, log: Log, color: NamedTextColor = defaultTextColor) {
        // if color is set, use that color
        // if not, alternate between two colors
        val msgColor =  if (color != defaultTextColor) color
                            else if (currentMessageColor) messageColorSwitch.first
                            else messageColorSwitch.second
        currentMessageColor = !currentMessageColor

        // send message to console
        plugin.server.consoleSender.sendMessage(
            tag.colorIfAbsent(NamedTextColor.DARK_AQUA).append(
                Component.text(" | ").color(NamedTextColor.DARK_GRAY)
                    .append(log.text.colorIfAbsent(msgColor)))) // colorized
    }

    /**
     * Get a logging instance
     *
     * @param instanceName The name of the service.
     * @param instanceTag The tag of the service.
     * @return A LoggingInstance object.
     */
    fun getLog(instanceName: String, instanceTag: Component) = LoggingInstance(this, instanceName, instanceTag)
}
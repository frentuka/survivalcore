package site.ftka.survivalcore.initless.logging.objects

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel

internal class Log(val color: NamedTextColor = NamedTextColor.YELLOW, val text: Component, val level: LogLevel) {
    val timestamp = System.currentTimeMillis()
    /*
        A single log.
     */
}
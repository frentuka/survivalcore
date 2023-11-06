package site.ftka.survivalcore.essentials.logging.objects

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.essentials.logging.LoggingEssential.LogLevel

class Log(val text: Component, val level: LogLevel) {
    val timestamp = System.currentTimeMillis()
    /*
        A single log.
     */
}
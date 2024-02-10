package site.ftka.survivalcore.initless.logging.objects

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel

class Log(val text: Component, val level: LogLevel) {
    val timestamp = System.currentTimeMillis()
    /*
        A single log.
     */
}
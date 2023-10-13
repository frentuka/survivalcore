package site.ftka.survivalcore.essentials.logging.objects

import site.ftka.survivalcore.essentials.logging.LoggingEssential.LogLevel

class Log(val text: String, val level: LogLevel) {
    val timestamp = System.currentTimeMillis()
    /*
        A single log.
     */
}
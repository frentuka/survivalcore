package site.ftka.survivalcore.services.logging.objects

import site.ftka.survivalcore.services.logging.LoggingService.LogLevel

class Log(val text: String, val level: LogLevel) {
    val timestamp = System.currentTimeMillis()
    /*
        A single log.
     */
}
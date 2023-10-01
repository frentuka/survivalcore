package site.ftka.proxycore.services.logging.objects

import site.ftka.proxycore.services.logging.LoggingService.LogLevel

class Log(val text: String, val level: LogLevel) {
    val timestamp = System.currentTimeMillis()
    /*
        A single log.
     */
}
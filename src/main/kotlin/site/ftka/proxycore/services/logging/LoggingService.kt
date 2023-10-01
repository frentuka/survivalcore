package site.ftka.proxycore.services.logging

import site.ftka.proxycore.MClass
import site.ftka.proxycore.services.logging.objects.Log
import site.ftka.proxycore.services.logging.objects.ServiceLogger
import site.ftka.proxycore.utils.textUtils.col

class LoggingService(private val plugin: MClass) {

    val logsFolderPath = plugin.dataDirectory.toAbsolutePath() + "\\logs"

    // Won't print logs with level above default.
    private val printableLogLevel = LogLevel.NORMAL

    fun print(tag: String?, log: Log) {
        // Don't print higher-than-intended log levels.
        if (log.level > printableLogLevel) return

        val message = "$tag  ${log.text}"
        plugin.server.consoleCommandSource.sendMessage(col(message)) // colorized
    }

    fun getLog(serviceName: String, serviceTag: String) = ServiceLogger(this, serviceName, serviceTag)

    enum class LogLevel(value: Int) {
        DEBUG(9), HIGH(3), NORMAL(2), LOW(1)
    }
}
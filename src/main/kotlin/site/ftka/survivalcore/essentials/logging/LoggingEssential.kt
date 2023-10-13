package site.ftka.survivalcore.essentials.logging

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.logging.objects.Log
import site.ftka.survivalcore.essentials.logging.objects.ServiceLogger
import site.ftka.survivalcore.utils.textUtils.col

class LoggingEssential(private val plugin: MClass) {

    val logsFolderPath = plugin.dataFolder.absolutePath + "\\logs"

    // Won't print logs with level above default.
    private val printableLogLevel = LogLevel.NORMAL

    fun print(tag: String?, log: Log) {
        // Don't print higher-than-intended log levels.
        if (log.level > printableLogLevel) return

        val message = "$tag  ${log.text}"
        plugin.server.consoleSender.sendMessage(col(message)) // colorized
    }

    fun getLog(serviceName: String, serviceTag: String) = ServiceLogger(this, serviceName, serviceTag)

    enum class LogLevel(value: Int) {
        DEBUG(9), HIGH(3), NORMAL(2), LOW(1)
    }
}
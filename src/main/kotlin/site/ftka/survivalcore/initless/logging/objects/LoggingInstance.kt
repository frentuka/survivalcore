package site.ftka.survivalcore.initless.logging.objects

import com.google.gson.GsonBuilder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.initless.logging.LoggingInitless
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import site.ftka.survivalcore.utils.dateUtils
import site.ftka.survivalcore.utils.objectsSizeUtils
import java.io.File

internal class LoggingInstance(private val loggingInitless: LoggingInitless, val name: String, var tag: Component) {

    /*
        LoggingInstance will be an instance to manage and create logs
        To log stuff, ask for a new LoggingInstance at the Logging initless.
     */

    // Lock-protected list of log entries for batching
    private val logList = mutableListOf<Log>()

    // Lock-free pipeline for incoming logs
    private val logChannel = Channel<Log>(Channel.UNLIMITED)

    // Platform-agnostic location where dumped logs will be stored
    private val logsFolder = File(loggingInitless.logsFolder, name)

    // Set size cap in bytes (64 KB)
    private val logsMaxSize = 1024 * 64
    private var logsSize = 0L

    // Sets next dump's timestamp. Refreshes after every dump.
    private var dumpTimestamp = System.currentTimeMillis()

    // Sets which log levels will be stored
    internal var dumpableLogLevels: Set<LogLevel> =
        setOf(LogLevel.DEBUG, LogLevel.HIGH, LogLevel.NORMAL, LogLevel.LOW)

    internal var printableLogLevels: Set<LogLevel> =
        setOf(LogLevel.DEBUG, LogLevel.HIGH, LogLevel.NORMAL, LogLevel.LOW)

    init {
        // Background worker to consume logs lock-free from the channel
        loggingInitless.coroutineScope.launch {
            for (log in logChannel) {
                processLog(log)
            }
        }
    }

    /*
        SubLogger
     */
    data class SubLogger(val logger: LoggingInstance, val tag: String) {
        fun log(text: String, level: LogLevel = LogLevel.NORMAL, color: NamedTextColor = logger.loggingInitless.defaultTextColor) {
            val newTag =
                logger.tag.append(Component.text(" {").color(NamedTextColor.WHITE))
                    .append(Component.text(tag).color(NamedTextColor.WHITE))
                    .append(Component.text("}").color(NamedTextColor.WHITE))
            logger.log(Component.text(text), level, newTag)
        }
    }

    // sub logger: logger for subsystems/subservices to include it's tag
    fun sub(tag: String): SubLogger {
        return SubLogger(this, tag)
    }

    /*
      ----------------------------------------------------------------------------------
    */

    fun log(text: String, level: LogLevel = LogLevel.NORMAL, color: NamedTextColor = loggingInitless.defaultTextColor) = log(Component.text(text), level, tag, color)

    fun log(text: Component, level: LogLevel = LogLevel.NORMAL, tag: Component = this.tag, color: NamedTextColor = loggingInitless.defaultTextColor) {
        val log = Log(color, text, level)

        // Print synchronously to keep exact temporal console log order
        if (log.level in printableLogLevels) {
            loggingInitless.print(tag, log, color)
        }

        // Notify external observers
        loggingInitless.notifyObservers(name, log)

        // Queue log entry lock-free via channel
        if (log.level in dumpableLogLevels) {
            logChannel.trySend(log)
        }
    }

    private fun processLog(log: Log) {
        var shouldDump = false
        synchronized(logList) {
            logList.add(log)
            logsSize += objectsSizeUtils.estimateStringSize(log.text.toString())
            if (logsSize > logsMaxSize) {
                shouldDump = true
            }
        }
        if (shouldDump) {
            dumpToStorage()
        }
    }

    /**
     * Triggers an asynchronous dump to storage via the plugin's coroutine scope
     */
    private fun dumpToStorage() {
        val logsCopy: List<Log>
        val currentDumpTimestamp: Long

        synchronized(logList) {
            if (logList.isEmpty()) return
            logsCopy = logList.toList()
            logList.clear()
            logsSize = 0
            currentDumpTimestamp = dumpTimestamp
            dumpTimestamp = System.currentTimeMillis()
        }

        // Using safe yyyy-MM-dd_HH-mm-ss timestamp (no colons) to be compatible with Windows filesystem
        val fileName = dateUtils.timeFormat(currentDumpTimestamp, "yyyy-MM-dd_HH-mm-ss") + "_${name}_log.json"

        // Log initiation (synchronous print)
        log(Component.text("Initiating log dump: $fileName"), LogLevel.DEBUG)

        loggingInitless.coroutineScope.launch {
            writeLogsToFile(logsCopy, fileName)
        }
    }

    /**
     * Triggers a synchronous flush of all remaining logs (used on shutdown)
     */
    fun flush() {
        logChannel.close()
        // Drain any remaining items that haven't been processed yet
        var result = logChannel.tryReceive()
        while (result.isSuccess) {
            val log = result.getOrThrow()
            synchronized(logList) {
                logList.add(log)
            }
            result = logChannel.tryReceive()
        }

        val logsCopy: List<Log>
        val currentDumpTimestamp: Long

        synchronized(logList) {
            if (logList.isEmpty()) return
            logsCopy = logList.toList()
            logList.clear()
            logsSize = 0
            currentDumpTimestamp = dumpTimestamp
            dumpTimestamp = System.currentTimeMillis()
        }

        val fileName = dateUtils.timeFormat(currentDumpTimestamp, "yyyy-MM-dd_HH-mm-ss") + "_${name}_log.json"
        writeLogsToFile(logsCopy, fileName)
    }

    /**
     * Retrieves a thread-safe snapshot of the currently buffered, not-yet-saved logs.
     */
    fun getBufferedLogs(): List<Log> {
        synchronized(logList) {
            return logList.toList()
        }
    }

    /**
     * Write logs to disk using structured file streams and UTF-8 encoding
     */
    private fun writeLogsToFile(logs: List<Log>, fileName: String) {
        if (logs.isEmpty()) return

        if (!logsFolder.exists()) {
            logsFolder.mkdirs()
        }

        val logsFile = File(logsFolder, fileName)

        try {
            java.io.FileOutputStream(logsFile).use { fos ->
                java.io.OutputStreamWriter(fos, java.nio.charset.StandardCharsets.UTF_8).use { osw ->
                    java.io.BufferedWriter(osw).use { bw ->
                        bw.write(toJson(logs))
                    }
                }
            }
        } catch (e: Exception) {
            loggingInitless.plugin.logger.severe("[SurvivalCore Logging] Failed to dump logs for $name to ${logsFile.absolutePath}")
            e.printStackTrace()
        }
    }

    private fun toJson(list: List<Log>): String {
        return loggingInitless.gson.toJson(list)
    }
}
package site.ftka.survivalcore.initless.logging.objects

import com.google.gson.GsonBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.initless.logging.LoggingInitless
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import site.ftka.survivalcore.utils.dateUtils
import site.ftka.survivalcore.utils.objectsSizeUtils
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

internal class LoggingInstance(private val loggingInitless: LoggingInitless, val name: String, var tag: Component) {

    /*
        LoggingInstance will be an instance to manage and create logs
        To log stuff, ask for a new LoggingInstance at the Logging initless.
     */

    // Map<Timestamp, Log message> | Used to know which logs to dump.
    private val logList = mutableListOf<Log>()

    // Location where dumped logs will be stored
    private val logsFolderPath = loggingInitless.logsFolderPath + "\\$name"

    // If logs list get too heavy, removes some.
    // Set size cap
    private var logsMaxSize = 1024*64 // 1/16th of a megabyte. Measurement is made in kilobytes.
    private var logsSize = 0L

    // Sets next dump's timestamp. Refreshes after every dump.
    private var dumpTimestamp = System.currentTimeMillis()

    // Sets which log levels will be stored
    internal var dumpableLogLevels: Set<LogLevel> =
        setOf(LogLevel.DEBUG, LogLevel.HIGH, LogLevel.NORMAL, LogLevel.LOW)

    internal var printableLogLevels: Set<LogLevel> =
        setOf(LogLevel.DEBUG, LogLevel.HIGH, LogLevel.NORMAL, LogLevel.LOW)

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

    @OptIn(DelicateCoroutinesApi::class)
    fun log(text: Component, level: LogLevel = LogLevel.NORMAL, tag: Component = this.tag, color: NamedTextColor = loggingInitless.defaultTextColor) {
        val log = Log(color, text, level)

        // print
        GlobalScope.launch {
            if (log.level in printableLogLevels)
                loggingInitless.print(tag, log, color)

            // process
            if (log.level in dumpableLogLevels) {
                logList.add(log)
                logsSize += objectsSizeUtils.estimateStringSize(text.toString())

                // dump log list if necessary
                if (logsSize > logsMaxSize) dumpToStorage()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun dumpToStorage() {
        // Filename example: 2023-09-22_23:51:33_PlayerData_log.json
        val fileName = dateUtils.timeFormat(dumpTimestamp, "yyyy-MM-dd_HH:mm:ss") + "_${name}_log.json"

        // Dump log
        log(Component.text("New log dump: $fileName"), LogLevel.DEBUG)

        // Grab a copy of logs list and clear the current one.
        val logsCopy = logList.toList()
        logList.clear()
        logsSize = 0

        // Write
        // Using Kotlin Coroutines and BufferedWriter to improve performance and prevent resource hogging
        GlobalScope.launch {
            val logsFolder = File(logsFolderPath)
            logsFolder.mkdirs()

            val logsFile = File("${logsFolderPath}\\${fileName}")

            val bufferedWriter: BufferedWriter

            try {
                bufferedWriter = BufferedWriter(FileWriter(logsFile))
                bufferedWriter.write(toJson(logsCopy))
                bufferedWriter.close()
            } catch (_: IOException) { }
        }

        // Reset dump timestamp
        dumpTimestamp = System.currentTimeMillis()
    }

    private fun toJson(list: List<Log>): String {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        return gsonPretty.toJson(list)
    }
}
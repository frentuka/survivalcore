package site.ftka.survivalcore.initless.logging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.objects.Log
import site.ftka.survivalcore.initless.logging.objects.LoggingInstance
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

/*
    LoggingInitless is a class that provides logging functionality
    for the plugin. It is used to log messages to the console, and
    to dump logs into files. It also provides a way to get a
    ServiceLogger object, which is used to log messages for a
    specific service.

    Should never be used externally.
 */
internal class LoggingInitless(internal val plugin: MClass) {

    enum class LogLevel(value: Int) {
        DEBUG(9), HIGH(3), NORMAL(2), LOW(1)
    }

    val defaultTextColor: NamedTextColor = NamedTextColor.YELLOW

    val logsFolder: File = File(plugin.dataFolder, "logs")
    val logsFolderPath: String = logsFolder.absolutePath

    // Coroutine scope for safe async disk writes, tied to the module's lifecycle
    val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Thread-safe Gson instance with Kyori TypeAdapter
    val gson: com.google.gson.Gson = com.google.gson.GsonBuilder()
        .registerTypeAdapter(Log::class.java, site.ftka.survivalcore.initless.logging.objects.LogSerializer())
        .setPrettyPrinting()
        .create()

    // Registry of all active logging instances to flush on shutdown
    private val activeInstances = ConcurrentLinkedQueue<LoggingInstance>()

    // External observers for log shipping
    interface LogObserver {
        fun onLog(loggerName: String, log: Log)
    }
    private val observers = ConcurrentLinkedQueue<LogObserver>()

    fun registerObserver(observer: LogObserver) {
        observers.add(observer)
    }

    internal fun notifyObservers(loggerName: String, log: Log) {
        observers.forEach { it.onLog(loggerName, log) }
    }

    init {
        // Asynchronous log retention worker
        initRetentionWorker()
    }

    private fun initRetentionWorker(maxFilesPerLogger: Int = 50) {
        coroutineScope.launch {
            if (!logsFolder.exists()) return@launch
            logsFolder.listFiles()?.forEach { loggerDir ->
                if (loggerDir.isDirectory) {
                    val logFiles = loggerDir.listFiles()?.filter { it.isFile && it.name.endsWith(".json") }
                        ?.sortedByDescending { it.lastModified() }
                    
                    if (logFiles != null && logFiles.size > maxFilesPerLogger) {
                        logFiles.drop(maxFilesPerLogger).forEach { it.delete() }
                    }
                }
            }
        }
    }

    /**
     * Admin query API to search recent logs for a specific logger.
     */
    fun queryLogs(loggerName: String, levelFilter: String = "all", dateFilter: String = "now", maxResults: Int = 50): List<Log> {
        val results = mutableListOf<Log>()
        
        val allowedLevels = if (levelFilter.equals("all", true)) {
            LogLevel.entries.toSet()
        } else {
            val mapped = when(levelFilter.lowercase()) {
                "info" -> LogLevel.NORMAL
                "warning" -> LogLevel.LOW
                "error" -> LogLevel.HIGH
                "debug" -> LogLevel.DEBUG
                else -> try { LogLevel.valueOf(levelFilter.uppercase()) } catch(e: Exception) { null }
            }
            if (mapped != null) setOf(mapped) else LogLevel.entries.toSet()
        }

        // Fetch not-yet-saved logs from the memory buffer if searching 'now'
        if (dateFilter.equals("now", true)) {
            val instance = activeInstances.find { it.name.equals(loggerName, true) }
            if (instance != null) {
                val buffered = instance.getBufferedLogs().filter { it.level in allowedLevels }
                results.addAll(buffered.reversed())
            }
        }

        val loggerDir = File(logsFolder, loggerName)
        if (!loggerDir.exists() || !loggerDir.isDirectory) return results.take(maxResults)

        var logFiles = loggerDir.listFiles()?.filter { it.isFile && it.name.endsWith(".json") } ?: emptyList()
        
        if (!dateFilter.equals("now", true)) {
            logFiles = logFiles.filter { it.name.contains(dateFilter) }
        }
        
        logFiles = logFiles.sortedByDescending { it.lastModified() }

        val type = object : com.google.gson.reflect.TypeToken<List<Log>>() {}.type

        for (file in logFiles) {
            if (results.size >= maxResults) break
            try {
                java.io.FileReader(file, java.nio.charset.StandardCharsets.UTF_8).use { reader ->
                    val logs: List<Log>? = gson.fromJson(reader, type)
                    if (logs != null) {
                        // Reverse so newest logs in the file are processed first
                        val filteredLogs = logs.filter { it.level in allowedLevels }.reversed()
                        results.addAll(filteredLogs)
                    }
                }
            } catch (e: Exception) {
                plugin.logger.severe("[SurvivalCore Logging] Failed to gracefully read log file during query: ${file.name}")
            }
        }
        return results.take(maxResults)
    }

    // below vars will switch one color to another for each message
    // to improve readability
    private val messageColorSwitch = Pair(TextColor.fromHexString("#FFFF22")!!, TextColor.fromHexString("#FFFFBB")!!)
    private var currentMessageColor = true // true = first, false = second
    private val colorLock = Any()

    fun print(tag: Component, log: Log, color: NamedTextColor = defaultTextColor) {
        // if color is set, use that color
        // if not, alternate between two colors inside a synchronized block for thread safety
        val msgColor = synchronized(colorLock) {
            val chosenColor = if (color != defaultTextColor) color
            else if (currentMessageColor) messageColorSwitch.first
            else messageColorSwitch.second
            currentMessageColor = !currentMessageColor
            chosenColor
        }

        // send message to console
        plugin.server.consoleSender.sendMessage(
            tag.colorIfAbsent(NamedTextColor.DARK_AQUA).append(
                Component.text(" | ").color(NamedTextColor.DARK_GRAY)
                    .append(log.text.colorIfAbsent(msgColor))
            )
        )
    }

    /**
     * Get a logging instance and register it
     *
     * @param instanceName The name of the service.
     * @param instanceTag The tag of the service.
     * @return A LoggingInstance object.
     */
    fun getLog(instanceName: String, instanceTag: Component): LoggingInstance {
        val instance = LoggingInstance(this, instanceName, instanceTag)
        activeInstances.add(instance)
        return instance
    }

    /**
     * Flush all pending logs in all active instances and cancel the coroutine scope on shutdown
     */
    fun shutdown() {
        for (instance in activeInstances) {
            instance.flush()
        }
        coroutineScope.cancel()
    }
}
package site.ftka.survivalcore.essentials.database

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.database.subservices.DatabaseHealthCheckSubservice
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import java.util.concurrent.CompletableFuture
import kotlin.Exception


class DatabaseEssential(private val plugin: MClass) {
    val logger = plugin.loggingInitless.getLog("DatabaseService", Component.text("Database").color(NamedTextColor.LIGHT_PURPLE))
    val api = DatabaseAPI(this)

    private var printStackTraces = true

    private val redis_conn_host = "redis://13548@127.0.0.1:6379/0"

    private var redisClient = RedisClient.create(RedisURI.create(redis_conn_host))
    private var redisConnection: StatefulRedisConnection<String, String>? = null

    private var healthCheck_ss = DatabaseHealthCheckSubservice(this, plugin)
    var health = true

    fun init() {
        logger.log("Initializing...")

        val connected = attemptConnectionInitialization()

        if (!connected) {
            logger.log("First connection attempt failed. Shutting down.", LogLevel.LOW, NamedTextColor.RED)
            plugin.server.shutdown()
        } else {
            logger.log("Successfully connected with database")
        }
    }

    fun restart() {
        logger.log("Restarting...")
        disconnect()

        redisConnection = null

        if (!attemptConnectionInitialization()) {
            logger.log("Connection failed on restart. Shutting down.", LogLevel.LOW, NamedTextColor.RED)
            plugin.server.shutdown()
        }
    }

    fun stop() {
        logger.log("Stopping...")
        disconnect()
    }

    // connects, adds listener and returns result
    private fun attemptConnectionInitialization(): Boolean {
        val connected = connect()
        redisConnection?.addListener(healthCheck_ss)

        return connected
    }

    private fun connect(): Boolean {
        return try {
            runBlocking {
                if (redisConnection == null || !redisConnection!!.isOpen)
                    redisConnection = redisClient.connect()
                true
            }
        } catch (e: Exception) { if (printStackTraces) e.printStackTrace(); false }
    }

    fun disconnect() {
        redisConnection?.closeAsync()
    }

    /*
            UTILITIES
     */

    fun ping(async: Boolean = true): CompletableFuture<Boolean> {
        // sync
        if (!async) return try {
            val syncCommands = redisConnection?.sync()
            CompletableFuture.completedFuture(syncCommands?.ping() == "PONG")
        } catch (e: Exception) { if (printStackTraces) e.printStackTrace(); CompletableFuture.completedFuture(false) }


        // async
        return try {
            val asyncCommands = redisConnection?.async()
            asyncCommands?.ping()?.thenApply { it == "PONG" }?.toCompletableFuture()
                ?: CompletableFuture.completedFuture(false)
        } catch (e: Exception) { if (printStackTraces) e.printStackTrace(); CompletableFuture.completedFuture(false) }
    }

    fun exists(key: String, async: Boolean = true): CompletableFuture<Boolean>? {
        // sync
        if (!async) return try {
            val syncCommands = redisConnection?.sync()
            CompletableFuture.completedFuture(syncCommands?.let{ it.exists(key) > 0 }) // true-false-null
        } catch (e: Exception) { if (printStackTraces) e.printStackTrace(); null }

        // async
        return try {
            val asyncCommands = redisConnection?.async()
            asyncCommands?.exists(key)?.thenApply { it > 0 }?.toCompletableFuture()
                ?: CompletableFuture.completedFuture(false)
        } catch (e: Exception) { if (printStackTraces) e.printStackTrace(); null }
    }

    fun get(key: String, async: Boolean = true): CompletableFuture<String>? {
        // sync
        if (!async) return try {
            val syncCommands = redisConnection?.sync()
            CompletableFuture.completedFuture(syncCommands?.get(key)) // value-null
        } catch (e: Exception) { if (printStackTraces) e.printStackTrace(); null }

        // async
        return try {
            val asyncCommands = redisConnection?.async()
            asyncCommands?.get(key)?.toCompletableFuture()
        } catch (e: Exception) { if (printStackTraces) e.printStackTrace(); null }
    }

    fun set(key: String, value: String, async: Boolean = true): CompletableFuture<Boolean> {
        // async
        if (!async) return try {
            val syncCommands = redisConnection?.sync()
            CompletableFuture.completedFuture(syncCommands?.set(key, value) == "OK")
        } catch (e: Exception) { if (printStackTraces) e.printStackTrace(); CompletableFuture.completedFuture(false) }

        // sync
        return try {
            val asyncCommands = redisConnection?.async()
            asyncCommands?.set(key, value)?.thenApply { it == "OK" }?.toCompletableFuture()
                ?: CompletableFuture.completedFuture(false)
        } catch (e: Exception) { if (printStackTraces) e.printStackTrace(); CompletableFuture.completedFuture(false) }
    }

}
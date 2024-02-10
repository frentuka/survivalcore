package site.ftka.survivalcore.essentials.database

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.database.subservices.DatabaseHealthCheckSubservice
import java.util.concurrent.CompletableFuture
import kotlin.Exception


class DatabaseEssential(private val plugin: MClass) {

    val logger = plugin.loggingInitless.getLog("DatabaseService", Component.text("Database").color(NamedTextColor.LIGHT_PURPLE))
    private var printStackTraces = true

    private val redis_conn_host = "redis://13548@127.0.0.1:6379/0"

    private var redisClient = RedisClient.create(RedisURI.create(redis_conn_host))
    private var redisConnection: StatefulRedisConnection<String, String>? = null

    private var stateListener = DatabaseHealthCheckSubservice(this, plugin)
    var health = true

    fun init() {
        logger.log("Attempting first redis connection. Local host ip is ${java.net.InetAddress.getLocalHost().hostAddress}")
        val connected = connect()
        redisConnection?.addListener(stateListener)

        if (!connected) {
            logger.log("First connection attempt failed. Shutting down.")
            plugin.server.shutdown()
        }
    }

    fun restart() {
        disconnect()

        redisConnection = null
        init()
    }

    private fun connect(): Boolean {
        return try {
            if (redisConnection == null || !redisConnection!!.isOpen)
                redisConnection = redisClient.connect()
            true
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
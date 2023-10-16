package site.ftka.survivalcore.services.database

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.logging.objects.ServiceLogger
import site.ftka.survivalcore.services.database.subservices.DatabaseHealthCheckSubservice
import site.ftka.survivalcore.utils.textUtils
import java.lang.Exception
import java.util.concurrent.CompletableFuture


class dbService(private val plugin: MClass) {

    val logger = plugin.loggingEssential.getLog("DatabaseService", "DB")

    private val redis_conn_host = "redis://13548@192.168.1.199:6379/0"
    private var redisClient = RedisClient.create(RedisURI.create(redis_conn_host))
    private var redisConnection: StatefulRedisConnection<String, String>? = null

    private var healthCheck_SS = DatabaseHealthCheckSubservice(this, plugin, 2000)
    var health = false

    fun init() {
        logger.log("Attempting first redis connection. Local host ip is ${java.net.InetAddress.getLocalHost().hostAddress}")


        if (!connect()) {
            logger.log("FIRST CONNECTION ATTEMPT FAILED. SHUTTING DOWN.")
            plugin.server.shutdown()
        }

    }

    fun restart() {
        disconnect()
        init()
    }

    fun connect(): Boolean {
        try {
            if (redisConnection == null || !redisConnection!!.isOpen) {
                redisConnection = redisClient.connect()
            }
        } catch (e: Exception) { return false }
        return true
    }

    fun disconnect() {
        redisConnection?.close()
    }

    fun isConnected(): Boolean {
        return redisConnection?.isOpen ?: false
    }

    /*
            SYNC
     */

    fun syncPing(): Boolean {
        try {
            val syncCommands = redisConnection?.sync()
            return syncCommands?.ping() == "PONG"
        }
        catch (e: Exception) { return false }

    }

    fun syncExists(key: String): Boolean {
        val syncCommands = redisConnection?.sync()
        return syncCommands?.let{ it.exists(key) > 0 } ?: false
    }

    fun syncGet(key: String): String? {
        val syncCommands = redisConnection?.sync()
        return syncCommands?.get(key)
    }

    fun syncSet(key: String, value: String): Boolean {
        val syncCommands = redisConnection?.sync()
        return syncCommands?.set(key, value) == "OK"
    }

    /*
            ASYNC
     */

    fun asyncPing(): CompletableFuture<Boolean> {
        val asyncCommands = redisConnection?.async()
        return asyncCommands?.ping()?.thenApply { it == "PONG" }?.toCompletableFuture()
            ?: CompletableFuture.completedFuture(false)
    }

    fun asyncExists(key: String): CompletableFuture<Boolean> {
        val asyncCommands = redisConnection?.async()
        return asyncCommands?.exists(key)?.thenApply { it > 0 }?.toCompletableFuture()
            ?: CompletableFuture.completedFuture(false)
    }

    fun asyncGet(key: String): CompletableFuture<String?> {
        val asyncCommands = redisConnection?.async()
        return asyncCommands?.get(key)?.toCompletableFuture()
            ?: CompletableFuture.completedFuture(null)
    }

    fun asyncSet(key: String, value: String): CompletableFuture<Boolean> {
        val asyncCommands = redisConnection?.async()
        return asyncCommands?.set(key, value)?.thenApply { it == "OK" }?.toCompletableFuture()
            ?: CompletableFuture.completedFuture(false)
    }

}
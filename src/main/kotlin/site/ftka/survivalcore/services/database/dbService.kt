package site.ftka.survivalcore.services.database

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import site.ftka.survivalcore.MClass
import java.util.concurrent.CompletableFuture

class dbService(private val plugin: MClass) {

    // Host a conectarse
    val redis_conn_host = "redis://13548@192.168.1.199:6379/0"

    private var redisConnectionVar: StatefulRedisConnection<String, String>

    init {
        println("Attempting redis connection. Local host ip is ${java.net.InetAddress.getLocalHost().hostAddress}")
        redisConnectionVar = getRedisConnection()
    }

    /*
        Control
        La conexión de Redis debería ser siempre síncrona, ya que...
     */
    fun getRedisConnection(): StatefulRedisConnection<String, String>  {
        val startTime = System.nanoTime()

        if (redisConnectionVar == null || redisConnectionVar.isOpen) {
            val elapsed = System.nanoTime() - startTime
            println("Redis Connection (cached): Time spent was $elapsed ns (${elapsed/1000000} ms)")
            return redisConnectionVar
        }
        val redisClient = RedisClient.create(redis_conn_host)
        redisConnectionVar = redisClient.connect()

        val elapsed = System.nanoTime() - startTime
        println("Redis Connection: Time spent was $elapsed ns (${elapsed/1000000} ms)")
        return redisConnectionVar
    }

    fun terminate() {
        redisConnectionVar.close()
    }


    /*
        Atajos
        Funciones cuya utilidad es ahorrar código repetitivo sobre las conexiones.
     */

    fun exists(key: String): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        future.completeAsync {
            val conn = getRedisConnection()
            val client = conn.async()

            client.exists(key).get() == 1L
        }
        return future
    }

    fun get(key: String): CompletableFuture<String?> {
        val future = CompletableFuture<String?>()

        exists(key).whenCompleteAsync{ result, _ ->
            if (!result) future.complete(null)
            return@whenCompleteAsync
        }

        val conn = getRedisConnection()
        val client = conn.async()

        client.get(key).whenCompleteAsync{result, thr ->
            future.complete(result)
            thr.printStackTrace()
        }

        return future
    }

    fun set(key: String, value: String): CompletableFuture<Boolean> {
        val conn = getRedisConnection()
        val client = conn.async()

        val setOperation = client.set(key, value)

        // return boolean to specify operation result
        val future = CompletableFuture<Boolean>()
        future.completeAsync{
            setOperation.get() == "OK" // Returns true if the set operation returns OK.
        }

        return future
    }

}
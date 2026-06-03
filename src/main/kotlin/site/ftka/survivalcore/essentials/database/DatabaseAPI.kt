package site.ftka.survivalcore.essentials.database

import java.util.concurrent.CompletableFuture

/*
    database-related stuff is only meant to happen inside the internal services
    it should never be public
 */
internal class DatabaseAPI(private val ess: DatabaseEssential) {

    fun ping(async: Boolean): CompletableFuture<Boolean>
        = ess.ping(async)

    fun exists(key: String): CompletableFuture<Boolean>
        = ess.exists(key)

    fun get(key: String, async: Boolean): CompletableFuture<String?>
        = ess.get(key, async)

    fun set(key: String, value: String, async: Boolean): CompletableFuture<Boolean>
        = ess.set(key, value, async)

    fun del(key: String, async: Boolean): CompletableFuture<Boolean>
        = ess.del(key, async)
}
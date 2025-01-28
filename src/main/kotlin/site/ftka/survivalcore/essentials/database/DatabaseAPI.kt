package site.ftka.survivalcore.essentials.database

/*
    database-related stuff is only meant to happen inside the internal services
    it should never be public
 */
internal class DatabaseAPI(private val ess: DatabaseEssential) {

    fun ping(async: Boolean)
        = ess.ping(async)

    fun exists(key: String)
        = ess.exists(key)

    fun get(key: String, async: Boolean)
        = ess.get(key, async)

    fun set(key: String, value: String, async: Boolean)
        = ess.set(key, value, async)

}
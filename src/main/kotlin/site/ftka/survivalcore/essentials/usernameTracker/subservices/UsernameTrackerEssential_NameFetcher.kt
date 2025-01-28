package site.ftka.survivalcore.essentials.usernameTracker.subservices

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import site.ftka.survivalcore.essentials.usernameTracker.UsernameTrackerEssential
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import java.net.HttpURLConnection
import java.net.URI
import java.util.UUID

internal class UsernameTrackerEssential_NameFetcher(private val ess: UsernameTrackerEssential) {

    private val logger = ess.logger.sub("NameFetcher")
    private val fetchURL = "https://api.minetools.eu/uuid/{uuid}"

    fun fetchNameAsync(uuid: UUID, callback: (String) -> Unit) {
        Thread {
            val name = fetchName(uuid)
            callback(name)
        }.start()
    }

    fun fetchName(uuid: UUID): String {
        val url = fetchURL.replace("{uuid}", uuid.toString())
        logger.log("Fetching name for $uuid from $url", LogLevel.HIGH)

        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.connect()

        val response = connection.inputStream.bufferedReader().use { it.readText() }
        // gather name from Json response
        val name = Gson().fromJson(response, Map::class.java)["name"] as String
        return name
    }

}
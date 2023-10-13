package site.ftka.survivalcore.services.playerdata.objects

import com.google.gson.GsonBuilder
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import java.util.UUID

open class PlayerData(val uuid: UUID, var username: String) {

    // language
    var lang: String = "en"

    // permissions
    var staffGroup: Int? = null
    var specialGroup: Int? = null
    var normalGroup: Int = 0
    var permissions: List<String> = listOf()


    // misc
    var updateTimestamp: Long = System.currentTimeMillis()


    fun toJson(): String {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        return gsonPretty.toJson(this)
    }
}
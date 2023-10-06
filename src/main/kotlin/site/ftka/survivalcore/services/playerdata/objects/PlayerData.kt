package site.ftka.survivalcore.services.playerdata.objects

import com.google.gson.GsonBuilder
import java.util.UUID

class PlayerData(val uuid: UUID, var username: String) {

    // language
    var lang: String = "en"

    // permissions
    var staffGroup: Int? = null
    var specialGroup: Int? = null
    var normalGroup: Int = 0
    var permissions: MutableList<String> = mutableListOf()

    fun toJson(): String {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        return gsonPretty.toJson(this)
    }
}
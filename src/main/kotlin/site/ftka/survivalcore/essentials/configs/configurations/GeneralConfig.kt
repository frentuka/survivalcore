package site.ftka.survivalcore.essentials.configs.configurations

import com.google.gson.GsonBuilder
import site.ftka.survivalcore.utils.serializers.SerializedLocation

class GeneralConfig {

    var version: Int = 1

    val PLAYER_FIRST_JOIN_SPAWN_LOCATION = SerializedLocation()
    val IJUSTWANTTOTEST = ":)"

    fun toJson(): String {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        return gsonPretty.toJson(this)
    }

}
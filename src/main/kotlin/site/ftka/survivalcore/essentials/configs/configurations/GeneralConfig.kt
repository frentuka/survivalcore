package site.ftka.survivalcore.essentials.configs.configurations

import com.google.gson.GsonBuilder
import site.ftka.survivalcore.utils.serializers.SerializedLocation

class GeneralConfig {

    var version: Int = 1

    val PLAYER_FIRST_JOIN_SPAWN_LOCATION = SerializedLocation()
    val IJUSTWANTTOTEST = ":)"

    /**
     * Converts the GeneralConfig object to a JSON string representation.
     *
     * This function uses Gson to serialize the current instance of GeneralConfig
     * into a pretty-printed JSON string.
     *
     * @return A string containing the JSON representation of the GeneralConfig object.
     */
    fun toJson(): String {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        return gsonPretty.toJson(this)
    }

}
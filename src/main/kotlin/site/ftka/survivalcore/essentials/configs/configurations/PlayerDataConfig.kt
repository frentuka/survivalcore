package site.ftka.survivalcore.essentials.configs.configurations

import com.google.gson.GsonBuilder

internal class PlayerDataConfig {

    var version: Int = 1

    val cache = cacheObj()
    class cacheObj {
        val timeToLiveMillis: Int = 1000*60*30
        val clockLoopTimeSecs: Long = 10
    }

    fun toJson(): String {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        return gsonPretty.toJson(this)
    }

}
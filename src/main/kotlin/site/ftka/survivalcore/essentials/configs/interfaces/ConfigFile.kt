package site.ftka.survivalcore.essentials.configs.interfaces

import com.google.gson.GsonBuilder

interface ConfigFile {

    fun toJson(): String {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        return gsonPretty.toJson(this)
    }

}


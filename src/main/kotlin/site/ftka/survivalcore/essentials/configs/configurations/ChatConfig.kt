package site.ftka.survivalcore.essentials.configs.configurations

import com.google.gson.GsonBuilder

internal class ChatConfig {

    var version = 1

    /**
     * {groupPrimaryColor}
     * {groupName}
     * {playerName}
     * {message}
     */
    val GLOBAL_MESSAGE_PREFIX = "{groupPrimaryColor}{groupName} <gray>{playerName}: <white>{message}"

    fun toJson(): String {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        return gsonPretty.toJson(this)
    }
}
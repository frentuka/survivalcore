package site.ftka.survivalcore.services.playerdata.objects

import com.google.gson.GsonBuilder
import site.ftka.survivalcore.services.playerdata.objects.modules.*
import java.util.UUID

data class PlayerData(val uuid: UUID) {

    var information:    PlayerInformation   = PlayerInformation()
    var state:          PlayerState         = PlayerState()
    var permissions:    PlayerPermissions   = PlayerPermissions()
    var settings:       PlayerSettings      = PlayerSettings()

    // misc
    var updateTimestamp: Long = System.currentTimeMillis()

    fun toJson(): String {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        return gsonPretty.toJson(this)
    }
}
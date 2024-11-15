package site.ftka.survivalcore.services.playerdata.objects

import com.google.gson.GsonBuilder
import site.ftka.survivalcore.services.playerdata.objects.modules.*
import java.util.UUID

data class PlayerData(val uuid: UUID) {

    var info = PlayerInformation(uuid)
    var state = PlayerState(uuid)
    var perms = PlayerPermissions(uuid)
    var settings = PlayerSettings(uuid)

    // misc
    var updateTimestamp: Long = System.currentTimeMillis()

    fun toJson(): String {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        return gsonPretty.toJson(this)
    }
}
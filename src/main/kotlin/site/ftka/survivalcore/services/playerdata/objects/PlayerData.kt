package site.ftka.survivalcore.services.playerdata.objects

import com.google.gson.GsonBuilder
import site.ftka.survivalcore.services.playerdata.objects.modules.PlayerInformation
import site.ftka.survivalcore.services.playerdata.objects.modules.PlayerLanguage
import site.ftka.survivalcore.services.playerdata.objects.modules.PlayerPermissions
import site.ftka.survivalcore.services.playerdata.objects.modules.PlayerState
import java.util.UUID

data class PlayerData(val uuid: UUID) {

    var info = PlayerInformation(uuid)
    var state = PlayerState(uuid, this)
    var perms = PlayerPermissions(uuid)
    var lang = PlayerLanguage(uuid)

    // misc
    var updateTimestamp: Long = System.currentTimeMillis()

    fun toJson(): String {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        return gsonPretty.toJson(this)
    }
}
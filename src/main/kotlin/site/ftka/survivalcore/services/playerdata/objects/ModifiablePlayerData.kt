package site.ftka.survivalcore.services.playerdata.objects

import site.ftka.survivalcore.services.playerdata.PlayerDataService
import java.util.*

class ModifiablePlayerData(private val service: PlayerDataService, uuid: UUID, username: String):
    PlayerData(uuid, username) {

    fun username() = username
    fun username(username: String) { this.username = username; update() }

    fun lang() = lang
    fun lang(lang: String) { this.lang = lang; update() }

    fun staffGroup() = staffGroup
    fun staffGroup(staffGroup: Int?) { this.staffGroup = staffGroup; update() }

    fun specialGroup() = specialGroup
    fun specialGroup(specialGroup: Int?) { this.specialGroup = specialGroup; update() }

    fun normalGroup() = normalGroup
    fun normalGroup(normalGroup: Int) { this.normalGroup = normalGroup; update() }

    fun permissions() = permissions
    fun permissions(permissions: List<String>) { this.permissions = permissions; update() }


    fun updateTimestamp() = updateTimestamp

    // update into PlayerDataService cache and database
    private fun update() {
        updateTimestamp = System.currentTimeMillis()
        service.update_ss.update(this)
    }
}
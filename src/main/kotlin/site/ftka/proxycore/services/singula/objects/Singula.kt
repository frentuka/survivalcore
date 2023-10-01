package site.ftka.proxycore.services.singula.objects

import site.ftka.proxycore.services.playerdata.objects.PlayerData
import site.ftka.proxycore.services.singula.SingulaService

class Singula(private var playerdata: PlayerData, private val service: SingulaService) {

    // Singula:
    // Data for this player was already gathered to create this object
    // So, the player for sure exists.
    // Can (or not) be online.

    // Getters

    /*
    getPlayerData
    get each value of playerdata
     */

    fun getPlayerData() = playerdata

    fun getUuid() = playerdata.uuid
    fun getUsername() = playerdata.username
    fun getLang() = playerdata.lang
    fun getStaffGroup() = playerdata.staffGroup
    fun getSpecialGroup() = playerdata.specialGroup
    fun getNormalGroup() = playerdata.normalGroup

    fun isOnline() = service.isOnline(playerdata.uuid)

    fun getProxyPlayer() = service.getProxyPlayer(playerdata.uuid)

    // Setters

    /*
    setPlayerData
    set each value of playerdata
     */

    fun setPlayerData(playerdata: PlayerData) {
        this.playerdata = playerdata
        service.update(this)
    }

    fun setUsername(username: String) {
        playerdata.username = username
        service.update(this)
    }

    fun setLang(lang: String) {
        playerdata.lang = lang
        service.update(this)
    }

    fun setStaffGroup(groupid: Int) {
        playerdata.staffGroup = groupid
        service.update(this)
    }

    fun setSpecialGroup(groupid: Int) {
        playerdata.specialGroup = groupid
        service.update(this)
    }

    fun setNormalGroup(groupid: Int) {
        playerdata.normalGroup = groupid
        service.update(this)
    }
}
package site.ftka.survivalcore.services.playerdata.objects.modules

import org.bukkit.entity.Player
import java.util.UUID

class PlayerInformation {

    var username: String = "{unknown}"
    var usernameHistory = mutableMapOf<Long, String>() // <Timestamp, Name>

    var lastConnection: Long? = null
    val firstConnection: Long = System.currentTimeMillis()

    fun updateValuesFromPlayer(player: Player) {
        // username
        this.username = player.name

        // usernameHistory
        /*
            FOR SOME FUCKING REASON THE IDE SAYS THAT THIS ELVIS OPERATOR IS USELESS
            BUT IF REMOVED, USERNAMEHISTORY COULD BE NULL
            DO NOT REMOVE IT.
         */
        usernameHistory ?: run { usernameHistory = mutableMapOf() }
        val max = if (usernameHistory.isEmpty()) null else usernameHistory.maxByOrNull { it.key }
        if (!max?.value.equals(this.username))
            usernameHistory[System.currentTimeMillis()] = this.username

        // lastConnection
        lastConnection = System.currentTimeMillis()
    }
}
package site.ftka.survivalcore.services.playerdata.objects.modules

import org.bukkit.entity.Player
import java.util.UUID

data class PlayerInformation(private val uuid: UUID) {

    var username: String = "{unknown}"
    val usernameHistory = mutableMapOf<Long, String>() // <Timestamp, Name>

    var lastConnection: Long? = null

    fun updateValuesFromPlayer(player: Player) {
        // username
        this.username = player.name

        // usernameHistory
        var updateUsernameHistory = false
        if (usernameHistory.isNotEmpty()) {
            val newestKey = usernameHistory.keys.max()
            if (!usernameHistory[newestKey].equals(player.name)) // If the newest key does NOT equal the current name
                updateUsernameHistory = true
        } else updateUsernameHistory = true
        if (updateUsernameHistory) usernameHistory[System.currentTimeMillis()] = player.name

        // lastConnection
        lastConnection = System.currentTimeMillis()

    }
}
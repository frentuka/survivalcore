package site.ftka.survivalcore.services.singula

import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.singula.singulas.OfflineSingula
import site.ftka.survivalcore.services.singula.singulas.Singula
import java.util.UUID
import kotlinx.coroutines.future.await

class SingulaAPI(private val plugin: MClass, private val svc: SingulaService) {

    /**
     * Get a Singula object from a Player
     * Player object must be online, otherwise
     * Singula object will only have default values
     *
     * @param player The player object
     */
    fun getSingula(player: Player): Singula {
        return Singula(plugin, player)
    }

    /**
     * Get a Singula object from a UUID
     * Could be offline, so Singula could be null
     *
     * @param uuid The UUID of the player
     */
    fun getSingula(uuid: UUID): Singula? {
        return plugin.server.getPlayer(uuid)?.let {
            getSingula(it)
        }
    }

    /**
     * Get an ISingula (OfflineSingula) object for an offline player
     *
     * @param uuid The player's UUID
     * @return The OfflineSingula object if UUID exists in database, null otherwise
     */
    suspend fun getOfflineSingula(uuid: UUID): OfflineSingula? {
        val exists = plugin.essentialsFwk.database.api.exists(uuid.toString()).await()
        if (exists != true) return null

        return OfflineSingula(plugin, uuid)
    }

}
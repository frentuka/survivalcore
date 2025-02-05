package site.ftka.survivalcore.services.singula

import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.singula.singulas.OfflineSingula
import site.ftka.survivalcore.services.singula.singulas.Singula
import java.util.UUID
import java.util.concurrent.CompletableFuture

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
     * Get a Singula object that's not online
     *
     * @param uuid The player's UUID
     * @return A CompletableFuture with the OfflineSingula object if UUID exists in database
     */
    fun getOfflineSingula(uuid: UUID): CompletableFuture<OfflineSingula?> {
        val future = CompletableFuture<OfflineSingula?>()

        future.completeAsync {
            if (plugin.essentialsFwk.database.api.exists(uuid.toString())?.get() != true)
                return@completeAsync null

            return@completeAsync OfflineSingula(plugin, uuid)
        }

        return future
    }

}
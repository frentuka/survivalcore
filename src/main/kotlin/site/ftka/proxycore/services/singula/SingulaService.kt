package site.ftka.proxycore.services.singula

import site.ftka.proxycore.MClass
import site.ftka.proxycore.services.playerdata.objects.PlayerData
import site.ftka.proxycore.services.singula.objects.Singula
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.jvm.optionals.getOrNull

class SingulaService(val plugin: MClass) {
    val services = plugin.services()

    private val singulaMap = mutableMapOf<UUID, Singula>()

    fun getOnline(uuid: UUID): Singula? {
        if (services.playerDataService.isCached(uuid)) return create(services.playerDataService.getCachedPlayer(uuid) ?: return null)
        return null
    }

    fun getOffline(uuid: UUID): CompletableFuture<Singula?> {
        val futureSingula = CompletableFuture<Singula?>()
        val futurePlayerData = services.playerDataService.pdiss.get(uuid)

        futurePlayerData.whenCompleteAsync { result, thr ->
            if (result == null) {
                futureSingula.complete(null)
                thr.printStackTrace()
                return@whenCompleteAsync
            }

            futureSingula.complete(create(result))
            thr.printStackTrace()
        }

        return futureSingula
    }

    fun update(singula: Singula) {

    }

    private fun create(playerdata: PlayerData) = Singula(playerdata, this)

    fun isOnline(uuid: UUID): Boolean = services.playerDataService.onlinePlayers.containsKey(uuid)

    fun getProxyPlayer(uuid: UUID) = plugin.server.getPlayer(uuid).getOrNull()
}
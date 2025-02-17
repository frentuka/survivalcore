package site.ftka.survivalcore.essentials.bossbar.subservices

import net.kyori.adventure.bossbar.BossBar
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.bossbar.BossBarEssential
import java.util.UUID

class BossBarEssential_BossBarSubservice(private val plugin: MClass, private val ess: BossBarEssential) {

    private val bossBars = mutableMapOf<UUID, MutableSet<String>>()

    fun getBossBars(uuid: UUID): Iterable<BossBar> {
        val player = plugin.server.getPlayer(uuid) ?: return setOf()
        return player.activeBossBars()
    }

    fun addBossBar(uuid: UUID, bar: BossBar) {
        val player = plugin.server.getPlayer(uuid) ?: return
        player.showBossBar(bar)
    }

    fun removeBossBar(uuid: UUID, bar: BossBar) {
        val player = plugin.server.getPlayer(uuid) ?: return
        player.hideBossBar(bar)
    }

}
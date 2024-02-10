package site.ftka.survivalcore.apps.PlayerSpawn.listeners

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.PlayerSpawn.PlayerSpawnApp
import site.ftka.survivalcore.initless.proprietaryEvents.annotations.PropEventHandler
import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropListener
import site.ftka.survivalcore.services.ServicesFramework
import site.ftka.survivalcore.services.playerdata.events.PlayerDataJoinEvent

class PlayerSpawnListener(private val app: PlayerSpawnApp,
                          private val services: ServicesFramework,
                          private val plugin: MClass): PropListener {

    @PropEventHandler
    fun onPlayerDataJoin(event: PlayerDataJoinEvent) {

    }

}
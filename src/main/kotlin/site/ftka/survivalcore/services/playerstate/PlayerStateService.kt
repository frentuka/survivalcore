package site.ftka.survivalcore.services.playerstate

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.ServicesCore
import site.ftka.survivalcore.services.playerstate.listeners.PlayerStateListener

class PlayerStateService(private val plugin: MClass, private val services: ServicesCore) {

    // Initialize listeners
    fun init() {
        plugin.initListener(PlayerStateListener(this, plugin))
    }

    // 1. Check for players online
    fun restart() {

    }

}
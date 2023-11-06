package site.ftka.survivalcore.services

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.language.LanguageService
import site.ftka.survivalcore.services.permissions.PermissionsService
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerstate.PlayerStateService

class ServicesCore(private val plugin: MClass) {

    var playerDataService = PlayerDataService(plugin, this)
    var playerStateService = PlayerStateService(plugin, this)
    var languageService = LanguageService(plugin, this)
    var permissionsService = PermissionsService(plugin, this)

    fun initAll() {
        playerDataService.init()
        playerStateService.init()
        languageService.init()
        permissionsService.init()
    }

    fun restartAll() {
        playerDataService.restart()
        playerStateService.restart()
        languageService.restart()
        permissionsService.restart()
    }

}
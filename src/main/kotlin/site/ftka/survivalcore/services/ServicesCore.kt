package site.ftka.survivalcore.services

import kotlinx.coroutines.delay
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.database.dbService
import site.ftka.survivalcore.services.language.LanguageService
import site.ftka.survivalcore.services.permissions.PermissionsService
import site.ftka.survivalcore.services.playerdata.PlayerDataService

class ServicesCore(private val plugin: MClass) {

    var dbService: dbService = dbService(plugin)
    var playerDataService = PlayerDataService(plugin)
    var languageService = LanguageService(plugin)
    var permissionsService = PermissionsService(plugin)

    fun initAll() {
        dbService.init()
        playerDataService.init()
        languageService.init()
        permissionsService.init()
    }

    fun restartAll() {
        dbService.restart()
        playerDataService.restart()
        languageService.restart()
        permissionsService.restart()
    }

}
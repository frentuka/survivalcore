package site.ftka.survivalcore.services

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chat.ChatService
import site.ftka.survivalcore.services.database.dbService
import site.ftka.survivalcore.services.logging.LoggingService
import site.ftka.survivalcore.services.permissions.PermissionsService
import site.ftka.survivalcore.services.playerdata.PlayerDataService

class ServicesCore(private val plugin: MClass) {

    var loggingService: LoggingService
    var dbService: dbService
    var playerDataService: PlayerDataService
    var permissionsService: PermissionsService
    var chatService: ChatService

    init {
        loggingService = LoggingService(plugin)
        dbService = dbService(plugin)
        playerDataService = PlayerDataService(plugin)
        permissionsService = PermissionsService(plugin)
        chatService = ChatService(plugin)
    }

    fun restart() {

    }

}
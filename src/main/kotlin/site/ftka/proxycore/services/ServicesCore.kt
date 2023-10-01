package site.ftka.proxycore.services

import site.ftka.proxycore.MClass
import site.ftka.proxycore.services.chat.ChatService
import site.ftka.proxycore.services.database.dbService
import site.ftka.proxycore.services.logging.LoggingService
import site.ftka.proxycore.services.permissions.PermissionsService
import site.ftka.proxycore.services.playerdata.PlayerDataService

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
package site.ftka.survivalcore.services.playerdata.subservices

import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.objects.PlayerData

internal class PlayerData_IntegritySubservice(private val service: PlayerDataService, private val plugin: MClass) {

    private val logger = service.logger.sub("Integrity")

    fun checkIntegrity(playerdata: PlayerData): Boolean {
        logger.log("Checking integrity for ${playerdata.uuid}", LogLevel.DEBUG)

        playerdata.state ?: run {
            logger.log("PlayerData ${playerdata.uuid} has no state module", LogLevel.LOW, NamedTextColor.RED)
            return false
        }

        playerdata.information ?: run {
            logger.log("PlayerData ${playerdata.uuid} has no information module", LogLevel.LOW, NamedTextColor.RED)
            return false
        }

        playerdata.permissions ?: run {
            logger.log("PlayerData ${playerdata.uuid} has no permissions module", LogLevel.LOW, NamedTextColor.RED)
            return false
        }

        playerdata.settings ?: run {
            logger.log("PlayerData ${playerdata.uuid} has no settings module", LogLevel.LOW, NamedTextColor.RED)
            return false
        }

        return true
    }



}
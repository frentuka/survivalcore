package site.ftka.survivalcore.services.playerdata.subservices

import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import java.io.File
import java.util.UUID

class PlayerData_BackupSubservice(private val service: PlayerDataService, private val plugin: MClass) {

    /*
        This subservice is meant to create String backups of Database information.
        When a PlayerData object is missing information from Database, this subservice
        will create a .json file backing up the Database information about this playerdata.

        If a module's variable name is modified and database's PlayerData objects are not updated,
        on login, they will get NullPointerExceptions regarding that module. This means that this module's data
        is lost. Using a String backup, this could be easily fixed, as crude information would not be lost.

        In order to accomplish this, the InputSubservice contains a Request Buffer that stores the last 10 requests
     */

    private val logger = service.logger.sub("Backup")
    private val absoluteBackupsFolderPath = "${service.baseFolderPath}/Backups"

    fun backupFromRequestBuffer(uuid: UUID) {
        logger.log("Starting backup for uuid ($uuid)")
        if (!service.inout_ss.getRequestBuffer().containsKey(uuid)) {
            logger.log("Request buffer does not contain uuid ($uuid). Aborting backup.", LogLevel.LOW, NamedTextColor.RED)
            return
        }

        val requestBuffer = service.inout_ss.getRequestBuffer().get(uuid)

        // Create backups folder
        val backupFolder = File(absoluteBackupsFolderPath)
        backupFolder.mkdirs()

        // Create backup files
        var backupFile = File("$absoluteBackupsFolderPath/${uuid}.json")

        // Choose an unexisting name
        // Needed because the same player could have multiple backups
        var copiesCount = 2
        while (backupFile.exists()) {
            backupFile = File("$absoluteBackupsFolderPath/${uuid}_$copiesCount.json")
            copiesCount++
        }

        // Create file
        backupFile.createNewFile()
        backupFile.writeText(requestBuffer ?: "NULL")

        logger.log("Backup for uuid ($uuid) has been created.", LogLevel.LOW, NamedTextColor.GREEN)
    }

}
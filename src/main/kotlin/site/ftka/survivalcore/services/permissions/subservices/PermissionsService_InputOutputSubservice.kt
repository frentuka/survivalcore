package site.ftka.survivalcore.services.permissions.subservices

import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import site.ftka.survivalcore.services.permissions.PermissionsService
import site.ftka.survivalcore.services.permissions.objects.PermissionGroup
import java.io.File

class PermissionsService_InputOutputSubservice(private val service: PermissionsService, private val plugin: MClass) {
    private val logger = service.logger.sub("InOut")

    private val groupsFolderAbsolutePath = "/${plugin.dataFolder.absolutePath}/groups"

    fun storeGroupsIntoMemory(clearGroupsMap: Boolean = true) {
        if (clearGroupsMap) { service.clearGroupsMap() }
        for (storedGroup in service.inout_ss.readGroupsFromStorage()) {
            service.setupGroup(storedGroup)
        }
    }

    fun storeGroupsIntoStorage() {
        for (group in service.getGroups()) {
            service.inout_ss.saveGroupIntoStorage(group)
        }
    }

    fun readGroupsFromStorage(): List<PermissionGroup> {
        logger.log("Attempting to read groups from storage", LogLevel.HIGH)

        val groupsFolderFile = File(groupsFolderAbsolutePath)
        if (!groupsFolderFile.exists()) { groupsFolderFile.mkdirs(); return listOf() } // does not exist
        if (!groupsFolderFile.isDirectory) { groupsFolderFile.deleteRecursively(); groupsFolderFile.mkdirs(); return listOf() } // something weird
        if (groupsFolderFile.listFiles()?.size == 0) return listOf() // empty

        val groups = mutableListOf<PermissionGroup>()

        for (possibleGroupFile in groupsFolderFile.listFiles()!!
            .filter{it.extension == "json" && !it.name.startsWith("INVALID")}) {

            try {
                val text = possibleGroupFile.readText()
                val group = service.fromJson(text)
                groups.add(group)

                logger.log("Detected group from groups folder: ${group.name}")

                // check if group name is right in the file name
                // file name should be {groupname}.json (all lowercase)
                val groupNameLowercase = group.name.lowercase()

                if (groupNameLowercase != possibleGroupFile.nameWithoutExtension) {
                    logger.log("Group file discrepancy detected: File name is ${possibleGroupFile.name} when it should be $groupNameLowercase. Renaming.", LogLevel.LOW, NamedTextColor.RED)
                    renameGroupFile(possibleGroupFile.nameWithoutExtension, groupNameLowercase)
                }

            } catch (e: Exception) {
                val fileName = possibleGroupFile.name
                val newFileName = "INVALID_$fileName"

                logger.log("Fatal error while attempting to read group file: ${fileName}. Renaming to ${newFileName}")

                val filePath = possibleGroupFile.absolutePath
                val newFilePath = filePath.replace(fileName, newFileName)
                val newFile = File(newFilePath)

                possibleGroupFile.renameTo(newFile)

                e.printStackTrace()
            }
        }

        return groups
    }


    fun saveGroupIntoStorage(group: PermissionGroup, overwriteIfExists: Boolean = true): Boolean {
        val groupsFolderFile = File(groupsFolderAbsolutePath)
        if (!groupsFolderFile.exists()) groupsFolderFile.mkdirs()
        if (!groupsFolderFile.isDirectory) return false // something weird

        val groupFileName = "${group.name}.json"
        val groupFileAbsolutePath = "/$groupsFolderFile/$groupFileName"
        val groupFile = File(groupFileAbsolutePath)

        if (groupFile.exists() && !overwriteIfExists) return false

        if (groupFile.exists()) groupFile.delete()
        groupFile.createNewFile()
        groupFile.writeText(group.toJson())

        return true
    }


    fun renameGroupFile(groupFileName: String, newGroupFileName: String): Boolean {
        val groupsFolderFile = File(groupsFolderAbsolutePath)
        if (!groupsFolderFile.exists()) return false
        if (!groupsFolderFile.isDirectory) return false

        val groupFileAbsolutePath = "/$groupsFolderAbsolutePath/$groupFileName.json"
        val groupFile = File(groupFileAbsolutePath)

        if (!groupFile.exists()) return false

        val newGroupFileAbsolutePath = "/$groupsFolderAbsolutePath/$newGroupFileName.json"
        val newGroupFile = File(newGroupFileAbsolutePath)

        return groupFile.renameTo(newGroupFile)
    }

}
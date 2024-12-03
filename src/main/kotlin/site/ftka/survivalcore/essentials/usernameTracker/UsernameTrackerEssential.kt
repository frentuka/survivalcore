package site.ftka.survivalcore.essentials.usernameTracker

import com.google.gson.Gson
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.EssentialsFramework
import site.ftka.survivalcore.essentials.usernameTracker.listeners.UsernameTrackerEssential_Listener
import site.ftka.survivalcore.essentials.usernameTracker.objects.UsernameTrackerMap
import site.ftka.survivalcore.essentials.usernameTracker.subservices.UsernameTrackerEssential_NameFetcher
import site.ftka.survivalcore.initless.logging.LoggingInitless.*
import java.io.File
import java.util.UUID

class UsernameTrackerEssential (private val essFwk: EssentialsFramework, private val plugin: MClass) {
    val logger = plugin.loggingInitless.getLog("UsernameTracker", Component.text("usernameTracker").color(NamedTextColor.GRAY))

    /*
        This class is meant to keep track of player's Usernames <-> UUIDs.
        This is needed for systems that will need to parse UUIDs from usernames.
        This essential should be pretty simple, as it only needs to keep track of the UUIDs.
        It's meant to be cheaper and simpler than creating a second UUIDs database.
     */

    /*
        Case scenario: New player joins the server.
        UUID is obviously unique.

        Possibility 1:
            Username is maybe already in this map by other UUID.
        Conclusion:
            It is safe to say that, if a NEW player has the same name than an OLD player,
            the OLD player has changed it's name.
        Action:
            Check for the OLD PLAYER's name using Mojang API.
            Replace old player's name with the new one.
            Insert new player's UUID and name.
     */


    private val folderAbsolutePath = "${plugin.dataFolder.absolutePath}/usernameTracker"
    private val filename = "usernameDatabase.json"

    private val listener = UsernameTrackerEssential_Listener(this)
    private val nameFetcher = UsernameTrackerEssential_NameFetcher(this)

    private var uuidsNamesMap = UsernameTrackerMap()

    fun usernameDatabase (): UsernameTrackerMap {
        return uuidsNamesMap
    }

    fun init() {
        logger.log("Initializing...")

        loadUsernamesDatabase()

        plugin.initListener(listener)
    }

    fun restart() {
        logger.log("Restarting...")

        uuidsNamesMap.clear()
        saveUsernamesDatabase()
        loadUsernamesDatabase()
    }

    fun stop() {
        logger.log("Stopping...")

        saveUsernamesDatabase()
    }

    private var inverseUUIDMap = mutableMapOf<String, UUID>()
    fun getUUID(name: String): UUID? {
        if (inverseUUIDMap.isEmpty())
            buildInverseUUIDMap()

        return inverseUUIDMap[name]
    }

    private fun buildInverseUUIDMap() =
        run { inverseUUIDMap = uuidsNamesMap.getMap().map { it.value to it.key }.toMap().toMutableMap() }

    private fun loadUsernamesDatabase() {
        logger.log("Loading usernames from database")
        val folder = File(folderAbsolutePath)
        if (!folder.exists()) folder.mkdirs()

        val file = File(folderAbsolutePath, filename)
        if (!file.exists()) {
            file.createNewFile()
            return
        }

        val json = file.readText()
        uuidsNamesMap = fromJson(json) ?: UsernameTrackerMap()

        buildInverseUUIDMap()

        logger.log("Loaded ${uuidsNamesMap.getMap().size} usernames from database")
    }

    private fun saveUsernamesDatabase() {
        val folder = File(folderAbsolutePath)
        if (!folder.exists()) folder.mkdirs()

        val file = File(folderAbsolutePath, filename)
        if (!file.exists()) file.createNewFile()

        val json = uuidsNamesMap.toJson()
        logger.log("Saving usernames database: $json", LogLevel.DEBUG)
        file.writeText(json)
    }

    fun addUsername(uuid: UUID, username: String) {
        if (!uuidsNamesMap.contains(uuid)) {
            uuidsNamesMap.addElement(uuid, username) // add new uuid-name pair
            logger.log("Added username $username with UUID $uuid", LogLevel.HIGH)
        }

        val duplicatedElements = uuidsNamesMap.getMap().filter { it.value == username && it.key != uuid }
        if (duplicatedElements.size > 0) { // there is more than 1 uuid with the same name
            logger.log("Found ${duplicatedElements.size} UUIDs using \"$username\" username. Fixing...", LogLevel.LOW, NamedTextColor.RED)

            for (element in duplicatedElements) {
                uuidsNamesMap.removeElement(element.key)

                val conflictive_uuid = element.key
                val wrongUsername = element.value

                nameFetcher.fetchNameAsync(conflictive_uuid) { newName ->
                    uuidsNamesMap.addElement(conflictive_uuid, newName)
                    logger.log("Replaced username $wrongUsername with $newName for UUID $conflictive_uuid", LogLevel.LOW, NamedTextColor.GREEN)
                }
            }

            // save file into storage
            saveUsernamesDatabase()
        }
    }

    fun getName(uuid: UUID): String? =
        uuidsNamesMap.getUsername(uuid)

    fun fromJson(json: String?): UsernameTrackerMap? = Gson().fromJson(json, UsernameTrackerMap::class.java)
}
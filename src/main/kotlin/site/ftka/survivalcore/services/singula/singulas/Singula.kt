package site.ftka.survivalcore.services.singula.singulas

import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.usernameTracker.UsernameTrackerEssential
import site.ftka.survivalcore.services.chat.ChatAPI
import site.ftka.survivalcore.services.chat.objects.ChatScreen
import site.ftka.survivalcore.services.language.LanguageAPI
import site.ftka.survivalcore.services.permissions.PermissionsAPI
import site.ftka.survivalcore.services.permissions.subservices.PermissionsService_PlayersSubservice
import site.ftka.survivalcore.services.playerdata.PlayerDataAPI
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.UUID

/**
 * Singula means "Individual" in Latin
 * This class represents a single player
 *
 * The Singula will be some kind of a "Player" object
 * from which you can interact with the player using all services
 *
 * This object should never be stored,
 * as player may have disconnected
 * and all methods will only throw default/void values.
 *
 * @param plugin Main plugin instance
 * @param uuid Player's UUID
 */
class Singula(private val plugin: MClass, val player: Player) {

    /*
        APIs
     */
    private val usernameTracker: UsernameTrackerEssential
        get() { return plugin.essentialsFwk.usernameTracker }

    private val chatAPI: ChatAPI
        get() { return plugin.servicesFwk.chat.api }

    private val langAPI: LanguageAPI
        get() { return plugin.servicesFwk.language.api }

    private val permsAPI: PermissionsAPI
        get() { return plugin.servicesFwk.permissions.api }

    private val playerDataAPI: PlayerDataAPI
        get() { return plugin.servicesFwk.playerData.api }

    /*
        variables
     */

    val username: String
        get() { return player.name }

    val uuid: UUID
        get() { return player.uniqueId }

    /*
        Online only
     */

    private fun isAvailable()
            = plugin.server.getPlayer(uuid) != null

        /*
            Chat
         */

    /**
     * Sends a private message to the player
     *
     * @param message The message to send
     * @param respectScreens If true, message won't be sent if player is inside an active screen
     */
    fun sendMessage(message: Component, respectScreens: Boolean = true)
        = chatAPI.sendPersonalMessage(uuid, message, respectScreens)

            /*
                Screens
             */

    /**
     * Shows a screen to the player
     *
     * @param screen The screen to show
     */
    fun showScreen(screen: ChatScreen)
        = chatAPI.showScreen(uuid, screen)

    /**
     * Refreshes the current active screen, if any
     *
     * @param page The page to set as current page
     */
    fun refreshScreen(page: String) {
        refreshScreen(chatAPI.getActiveScreen(uuid)?.name ?: return, page)
    }

    /**
     * Refreshes a screen, if active
     *
     * @param screenName The name of the screen
     * @param page The page to set as current page
     */
    fun refreshScreen(screenName: String, page: String)
        = chatAPI.refreshScreen(uuid, screenName, page)

    fun showOrRefreshScreen(screen: ChatScreen, page: String = "home")
        = chatAPI.showOrRefreshScreen(uuid, screen, page)

    /**
     * Stops the current active screen, if any
     */
    fun stopScreen()
        = chatAPI.stopAnyScreen(uuid)

    /**
     * Stops a screen, if active
     *
     * @param screenName The name of the screen to stop
     */
    fun stopScreen(screenName: String)
        = chatAPI.stopScreen(uuid, screenName)

            /*
                Channels
             */

    /**
     *  Gets the active channels of the player
     *
     *  @return A list of active channels
     */
    fun getActiveChannels()
            = chatAPI.getPlayerActiveChannels(uuid)

    /**
     * Adds a channel to the player's active channels
     *
     * @param channel The channel to add
     */
    fun addActiveChannel(channel: String)
        = chatAPI.addActiveChannel(uuid, channel)

    /**
     * Removes a channel from the player's active channels
     *
     * @param channel The channel to remove
     */
    fun removeActiveChannel(channel: String)
        = chatAPI.removeActiveChannel(uuid, channel)

    /**
     * Gets the chat log of the player
     *
     * @param entries The number of entries to get
     * @return A list of chat entries
     */
    fun getChatLog(entries: Int)
        = chatAPI.getPlayerChatLog(uuid, entries)

        /*
            Language
         */

    /**
     * Gets the language of the player
     *
     * @return The language pack of the player
     */
    fun getLanguage()
        = langAPI.playerLanguagePack(uuid)

        /*
            Permissions
         */

            /*
                Permissions (redundantly)
             */

    /**
     * Checks if the player has a permission
     *
     * @param permission The permission to check
     * @return True if player has the permission
     */
    fun hasPermission(permission: String)
        = permsAPI.player_hasPerm_locally(uuid, permission)

    /**
     * Gets the permissions of the player
     *
     * @return A set of permissions
     */
    fun getPermissions(): Set<String> {
        if (isAvailable())
            return permsAPI.player_getPerms(uuid).get() ?: setOf()
        return setOf()
    }

    /**
     * Adds a permission to the player.
     *
     * @param permission The permission to add
     * @return The result of the operation
     */
    fun addPermission(permission: String): PermissionsService_PlayersSubservice.Permissions_addPermissionResult {
        if (isAvailable())
            return runBlocking { permsAPI.player_addPerm(uuid, permission) }
        return PermissionsService_PlayersSubservice.Permissions_addPermissionResult.FAILURE_PLAYER_UNAVAILABLE
    }

    /**
     * Removes a permission from the player.
     *
     * @param permission The permission to remove
     * @return The result of the operation
     */
    fun removePermission(permission: String): PermissionsService_PlayersSubservice.Permissions_removePermissionResult {
        if (isAvailable())
            return runBlocking { permsAPI.player_removePerm(uuid, permission) }
        return PermissionsService_PlayersSubservice.Permissions_removePermissionResult.FAILURE_PLAYER_UNAVAILABLE
    }

            /*
                Groups
             */

    /**
     * Gets the groups of the player
     *
     * @return A set of groups UUIDs
     */
    fun getGroups(): Set<UUID> {
        if (isAvailable())
            return permsAPI.player_getGroups(uuid).get() ?: setOf()
        return setOf()
    }

    /**
     * Gets the display group of the player
     *
     * @return The display group UUID
     */
    fun getDisplayGroup(): UUID? {
        if (isAvailable())
            return permsAPI.player_getDisplayGroup(uuid).get()
        return null
    }

    /**
     * Adds a group to the player
     *
     * @param group The group to add
     * @return The result of the operation
     */
    fun addGroup(group: UUID): PermissionsService_PlayersSubservice.Permissions_addGroupResult {
        if (isAvailable())
            return runBlocking { permsAPI.player_addGroup(uuid, group) }
        return PermissionsService_PlayersSubservice.Permissions_addGroupResult.FAILURE_PLAYER_UNAVAILABLE
    }

    /**
     * Removes a group from the player
     *
     * @param group The group to remove
     * @return The result of the operation
     */
    fun removeGroup(group: UUID): PermissionsService_PlayersSubservice.Permissions_removeGroupResult {
        if (isAvailable())
            return runBlocking { permsAPI.player_removeGroup(uuid, group) }
        return PermissionsService_PlayersSubservice.Permissions_removeGroupResult.FAILURE_PLAYER_UNAVAILABLE
    }

    /**
     * Sets the display group of the player
     *
     * @param group The group to set as display group
     * @return The result of the operation
     */
    fun setDisplayGroup(group: UUID): PermissionsService_PlayersSubservice.Permissions_setDisplayGroupResult {
        if (isAvailable())
            return runBlocking { permsAPI.player_setDisplayGroup(uuid, group) }
        return PermissionsService_PlayersSubservice.Permissions_setDisplayGroupResult.FAILURE_PLAYER_UNAVAILABLE
    }

        /*
            PlayerData
         */

    /**
     * Gets the player data of the player
     *
     * @return The PlayerData object
     */
    fun getPlayerData()
        = playerDataAPI.getPlayerData_locally(uuid)

}
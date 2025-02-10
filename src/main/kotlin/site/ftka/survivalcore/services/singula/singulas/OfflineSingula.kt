package site.ftka.survivalcore.services.singula.singulas

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.usernameTracker.UsernameTrackerEssential
import site.ftka.survivalcore.essentials.chat.ChatAPI
import site.ftka.survivalcore.services.language.LanguageAPI
import site.ftka.survivalcore.services.permissions.PermissionsAPI
import site.ftka.survivalcore.services.playerdata.PlayerDataAPI
import java.util.UUID

/**
 * A Singula object that represents an offline player
 * This object will be used to interact with database/cached data
 *
 * Can be stored but it's not recommended
 *
 * @param plugin Main plugin instance
 * @param uuid Player's UUID
 */
class OfflineSingula(private val plugin: MClass, val uuid: UUID) {

    /*
        APIs
     */

    private val usernameTracker: UsernameTrackerEssential
        get() { return plugin.essentialsFwk.usernameTracker }

    private val chatAPI: ChatAPI
        get() { return plugin.essentialsFwk.chat.api }

    private val langAPI: LanguageAPI
        get() { return plugin.servicesFwk.language.api }

    private val permsAPI: PermissionsAPI
        get() { return plugin.servicesFwk.permissions.api }

    private val playerDataAPI: PlayerDataAPI
        get() { return plugin.servicesFwk.playerData.api }

    /*
        variables
     */

    val username: String?
        get() { return usernameTracker.getName(uuid) }

    /*
        Permissions
     */

    /**
     * Checks if the player has a permission
     *
     * @param permission Permission to check
     * @return Whether the player has the permission
     */
    fun hasPermission(permission: String)
        = permsAPI.player_hasPerm(uuid, permission)

    /**
     * Gets the player's permissions
     *
     * @return The permissions the player has
     */
    fun getPermissions()
        = permsAPI.player_getPerms(uuid)

    /**
     * Adds a permission to the player
     *
     * @param permission Permission to add
     * @return The result of the operation
     */
    suspend fun addPermission(permission: String)
        = permsAPI.player_addPerm(uuid, permission)

    /**
     * Removes a permission from the player
     *
     * @param permission Permission to remove
     * @return The result of the operation
     */
    suspend fun removePermission(permission: String)
        = permsAPI.player_removePerm(uuid, permission)

    /**
     * Gets the player's groups
     *
     * @return The groups the player is in
     */
    fun getGroups()
        = permsAPI.player_getGroups(uuid)

    /**
     * Gets the player's display group
     *
     * @return The display group
     */
    fun getDisplayGroup()
        = permsAPI.player_getDisplayGroup(uuid)

    /**
     * Adds a group to the player
     *
     * @param group Group to add
     * @return The result of the operation
     */
    suspend fun addGroup(group: String)
        = permsAPI.player_addGroup(uuid, group)

    /**
     * Removes a group from the player
     *
     * @param group Group to remove
     * @return The result of the operation
     */
    suspend fun removeGroup(group: String)
        = permsAPI.player_removeGroup(uuid, group)

    /**
     * Sets the player's display group
     *
     * @param group Group to set
     */
    suspend fun setDisplayGroup(group: String)
        = permsAPI.player_setDisplayGroup(uuid, group)

    /*
        PlayerData
     */

    /**
     * Gets the player's data
     *
     * @return PlayerData object
     */
    fun getPlayerData()
        = playerDataAPI.getPlayerData(uuid)

}
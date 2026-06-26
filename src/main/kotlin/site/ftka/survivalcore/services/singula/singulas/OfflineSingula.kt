package site.ftka.survivalcore.services.singula.singulas

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.usernameTracker.UsernameTrackerEssential
import site.ftka.survivalcore.essentials.chat.ChatAPI
import site.ftka.survivalcore.services.language.LanguageAPI
import site.ftka.survivalcore.services.permissions.PermissionsAPI
import site.ftka.survivalcore.services.playerdata.PlayerDataAPI
import site.ftka.survivalcore.services.permissions.subservices.PermissionsService_PlayersSubservice
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.UUID
import kotlinx.coroutines.future.await

/**
 * A Singula object that represents an offline player
 * This object will be used to interact with database/cached data
 *
 * Can be stored but it's not recommended
 *
 * @param plugin Main plugin instance
 * @param uuid Player's UUID
 */
class OfflineSingula(private val plugin: MClass, override val uuid: UUID) : ISingula {

    /*
        APIs
     */

    private val usernameTracker: UsernameTrackerEssential = plugin.essentialsFwk.usernameTracker
    private val chatAPI: ChatAPI = plugin.essentialsFwk.chat.api
    private val langAPI: LanguageAPI = plugin.servicesFwk.language.api
    private val permsAPI: PermissionsAPI = plugin.servicesFwk.permissions.api
    private val playerDataAPI: PlayerDataAPI = plugin.servicesFwk.playerData.api

    /*
        variables
     */

    override val username: String?
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
    override suspend fun hasPermission(permission: String): Boolean
        = permsAPI.player_hasPerm(uuid, permission).await() ?: false

    /**
     * Gets the player's permissions
     *
     * @return The permissions the player has
     */
    override suspend fun getPermissions(): Set<String>
        = permsAPI.player_getPerms(uuid).await() ?: setOf()

    /**
     * Adds a permission to the player
     *
     * @param permission Permission to add
     * @return The result of the operation
     */
    override suspend fun addPermission(permission: String): PermissionsService_PlayersSubservice.Permissions_addPermissionResult
        = permsAPI.player_addPerm(uuid, permission)

    /**
     * Removes a permission from the player
     *
     * @param permission Permission to remove
     * @return The result of the operation
     */
    override suspend fun removePermission(permission: String): PermissionsService_PlayersSubservice.Permissions_removePermissionResult
        = permsAPI.player_removePerm(uuid, permission)

    /**
     * Gets the player's groups
     *
     * @return The groups the player is in
     */
    override suspend fun getGroups(): Set<UUID>
        = permsAPI.player_getGroups(uuid).await() ?: setOf()

    /**
     * Gets the player's display group
     *
     * @return The display group
     */
    override suspend fun getDisplayGroup(): UUID?
        = permsAPI.player_getDisplayGroup(uuid).await()

    /**
     * Adds a group to the player
     *
     * @param group Group to add
     * @return The result of the operation
     */
    override suspend fun addGroup(group: String): PermissionsService_PlayersSubservice.Permissions_addGroupResult
        = permsAPI.player_addGroup(uuid, group)
    override suspend fun addGroup(group: UUID): PermissionsService_PlayersSubservice.Permissions_addGroupResult
        = permsAPI.player_addGroup(uuid, group)

    /**
     * Removes a group from the player
     *
     * @param group Group to remove
     * @return The result of the operation
     */
    override suspend fun removeGroup(group: String): PermissionsService_PlayersSubservice.Permissions_removeGroupResult
        = permsAPI.player_removeGroup(uuid, group)
    override suspend fun removeGroup(group: UUID): PermissionsService_PlayersSubservice.Permissions_removeGroupResult
        = permsAPI.player_removeGroup(uuid, group)

    /**
     * Sets the player's display group
     *
     * @param group Group to set
     */
    override suspend fun setDisplayGroup(group: String): PermissionsService_PlayersSubservice.Permissions_setDisplayGroupResult
        = permsAPI.player_setDisplayGroup(uuid, group)
    override suspend fun setDisplayGroup(group: UUID): PermissionsService_PlayersSubservice.Permissions_setDisplayGroupResult
        = permsAPI.player_setDisplayGroup(uuid, group)

    /*
        PlayerData
     */

    /**
     * Gets the player's data
     *
     * @return PlayerData object
     */
    override suspend fun getPlayerData(): PlayerData?
        = playerDataAPI.getPlayerData(uuid).await()

}
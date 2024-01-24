package site.ftka.survivalcore.services.chat

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chat.objects.ChatChannel
import java.util.UUID

class ChatService(var plugin: MClass) {

    /*
        ChatService

        This service is meant to fully control
        every player's chat.

        HOW TO BE USED:
        Ask for a channel. Let's say, "Staff" using
     */

    // being UUID, String = player's UUID, channel name (usually named by player's UUID)
    // done this way so that other channels like global, staff, faction
    // can be called "global", "staff" or "faction_{factionName}"
    private val playerChannelMap = mutableMapOf<UUID, String>()
    private val channelsMap = mutableMapOf<String, ChatChannel>()

    // $ to prevent collision with playernames
    private val GLOBAL_CHANNEL_NAME: String = "\$global"
    private val STAFF_CHANNEL_NAME: String = "\$staff"
    private val CLAN_CHANNEL_NAME_PREFIX = "\$clanchannel_$"

    fun init() {
        createElementalChannels()
    }

    // 1. reset all chat data
    // 2. create elemental channels
    // 3. create all needed channels (player, clan, etc)
    fun restart() {
        // 1.
        channelsMap.clear()
        playerChannelMap.clear()

        // 2.
        createElementalChannels()

        // 3. players
        for (player in plugin.server.onlinePlayers)
            registerChannel(player.uniqueId.toString(), true, player.uniqueId)
    }

    // create elemental channels:
    // Global, Staff
    private fun createElementalChannels() {
        var elementalChannelSettings = ChatChannelSettings()
        elementalChannelSettings.maxChatEntries = 100
        elementalChannelSettings.messageTimeoutSeconds = 7200

        val globalChannel = ChatChannel(this, GLOBAL_CHANNEL_NAME, elementalChannelSettings)
        val staffChannel = ChatChannel(this, STAFF_CHANNEL_NAME, elementalChannelSettings)

        channelsMap[globalChannel.name] = globalChannel
        channelsMap[staffChannel.name] = staffChannel
    }

    /**
     * This method is ONLY FOR INTERNAL USAGE.
     *
     * If you need to communicate with a player,
     * communicate using the player's channel.
     *
     * Get the player's channel using the getPlayerChannel method
     */
    fun sendRawMessageToPlayer(uuid: UUID, message: Component) {
        // get online player
        val player = plugin.server.getPlayer(uuid) ?: return

        // send the message
        player.sendMessage(message)
    }


    /*
            PUBLIC USAGE ZONE
     */


    fun registerChannel(name: String, isPlayerChannel: Boolean = true, playerUUID: UUID? = null): ChatChannel {
        val channel = ChatChannel(this, name)

        channelsMap.put(name, channel)
        if (isPlayerChannel && playerUUID != null)
            playerChannelMap[playerUUID] = channel.name

        return channel
    }

    fun getChannel(name: String): ChatChannel? = channelsMap[name]

    fun getGlobalChannel() = getChannel(GLOBAL_CHANNEL_NAME)

    fun getStaffChannel() = getChannel(STAFF_CHANNEL_NAME)

    fun getPlayerChannel(uuid: UUID, createIfNotExists: Boolean = true): ChatChannel? {
        if (playerChannelMap.containsKey(uuid) && channelsMap.containsKey(uuid.toString()))
            return channelsMap[uuid.toString()]

        // Apparently, it does not exist
        if (createIfNotExists)
            return registerChannel(uuid.toString(), true, uuid)

        return null
    }

}

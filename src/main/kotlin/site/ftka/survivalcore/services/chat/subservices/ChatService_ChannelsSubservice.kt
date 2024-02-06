package site.ftka.survivalcore.services.chat.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chat.ChatService
import site.ftka.survivalcore.services.chat.objects.ChatChannel
import java.util.*

class ChatService_ChannelsSubservice(private val service: ChatService, private val plugin: MClass) {

    private val channelsMap = mutableMapOf<String, ChatChannel>()

    // being UUID, String = player's UUID, channel name (usually named by player's UUID)
    // done this way so that other channels like global, staff, faction
    // can be called "global", "staff" or "faction_{factionName}"
    private val playerChannelMap = mutableMapOf<UUID, String>()

    // $ to prevent collision with playernames
    private val GLOBAL_CHANNEL_NAME: String = "\$global"
    private val STAFF_CHANNEL_NAME: String = "\$staff"
    private val CLAN_CHANNEL_NAME_PREFIX = "\$clanchannel_$"

    fun clearMaps() {
        channelsMap.clear()
        playerChannelMap.clear()
    }

    // create elemental channels:
    // Global, Staff
    fun createElementalChannels() {
        var elementalChannelSettings = ChatChannel.ChatChannelSettings()
        elementalChannelSettings.maxStoredChatEntries = 100

        val globalChannel = ChatChannel(service, GLOBAL_CHANNEL_NAME, elementalChannelSettings)
        val staffChannel = ChatChannel(service, STAFF_CHANNEL_NAME, elementalChannelSettings)

        channelsMap[globalChannel.name] = globalChannel
        channelsMap[staffChannel.name] = staffChannel
    }

    fun registerChannel(name: String, isPlayerChannel: Boolean = true, playerUUID: UUID? = null): ChatChannel {
        val channel = ChatChannel(service, name)

        channelsMap.put(name, channel)
        if (isPlayerChannel && playerUUID != null)
            playerChannelMap[playerUUID] = channel.name

        return channel
    }

    // gets

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
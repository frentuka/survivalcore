package site.ftka.survivalcore.services.chat.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chat.ChatService
import site.ftka.survivalcore.services.chat.objects.ChatChannel
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ChatService_ChannelsSubservice(private val service: ChatService, private val plugin: MClass) {

    private val channelsMap = mutableMapOf<String, ChatChannel>()
    private val playersActiveChannels = mutableMapOf<UUID, Set<String>>()

    // "$" to prevent collision with playernames
    private val GLOBAL_CHANNEL_NAME: String = "\$global"
    private val STAFF_CHANNEL_NAME: String = "\$staff"
    private val CLAN_CHANNEL_NAME_PREFIX = "\$clan_"

    fun clearMaps() {
        channelsMap.clear()
    }

    // create elemental channels:
    // Global, Staff
    fun createElementalChannels() {
        val elementalChannelSettings = ChatChannel.ChatChannelSettings()
        elementalChannelSettings.maxStoredChatEntries = 100 // full chat page
        elementalChannelSettings.timeoutAfterSeconds = -1   // no timeout

        val globalChannel = ChatChannel(service, GLOBAL_CHANNEL_NAME, elementalChannelSettings)
        val staffChannel = ChatChannel(service, STAFF_CHANNEL_NAME, elementalChannelSettings)

        channelsMap[globalChannel.name] = globalChannel
        channelsMap[staffChannel.name] = staffChannel
    }

    fun registerChannel(name: String, isPlayerChannel: Boolean = true, playerUUID: UUID? = null): ChatChannel {
        val channel = ChatChannel(service, name)

        startPurgeDataTimer()

        channelsMap.put(name, channel)
        if (isPlayerChannel && playerUUID != null)
            channelsMap[playerUUID.toString()] = channel

        return channel
    }

    fun modifyChannel(channelName: String, modification: (ChatChannel) -> Unit) {
        val channel = channelsMap[channelName] ?: return
        modification(channel)
        channelsMap[channelName] = channel
    }

    // gets

    fun existsChannel(channelName: String): Boolean =
        channelsMap.containsKey(channelName)

    fun getChannel(name: String): ChatChannel? = channelsMap[name]

    fun getGlobalChannel() = getChannel(GLOBAL_CHANNEL_NAME)

    fun getStaffChannel() = getChannel(STAFF_CHANNEL_NAME)

    fun getPlayerChannel(uuid: UUID, createIfNotExists: Boolean = true): ChatChannel? {
        if (channelsMap.containsKey(uuid.toString()))
            return channelsMap[uuid.toString()]

        // Apparently, it does not exist
        if (createIfNotExists)
            return registerChannel(uuid.toString(), true, uuid)

        return null
    }

    /*
        active channels
     */

    fun getActiveChannels(uuid: UUID): Set<String> {
        return playersActiveChannels[uuid] ?: setOf()
    }

    fun setActiveChannels(uuid: UUID, channels: Set<String>) {
        playersActiveChannels[uuid] = channels
    }

    fun addActiveChannel(uuid: UUID, channel: String) {
        if (!playersActiveChannels.containsKey(uuid))
            playersActiveChannels[uuid] = mutableSetOf()

        playersActiveChannels[uuid] = playersActiveChannels[uuid]!!.plus(channel)
    }

    fun removeActiveChannel(uuid: UUID, channel: String) {
        if (!playersActiveChannels.containsKey(uuid))
            return

        playersActiveChannels[uuid] = playersActiveChannels[uuid]!!.minus(channel)
    }

    fun removeActiveChannels(uuid: UUID) {
        playersActiveChannels.remove(uuid)
    }

    /*
        data purge timeout
     */

    // clock that will do purgeDataCheck every 10 seconds
    private var purgeScheduledFuture: ScheduledFuture<*>? = null
    private var playersActiveChannelsTimeoutMap = mutableMapOf<UUID, Int>()

    fun startPurgeDataTimer() {
        if (purgeScheduledFuture != null)
            return

        purgeScheduledFuture = plugin.globalScheduler.scheduleAtFixedRate({
            purgeDataCheck()
        }, 10, 10, TimeUnit.SECONDS)
    }

    // will remove data from channels if they dont update in certain amount of time
    fun purgeDataCheck() {
        // check all channels
        for (channelName in channelsMap.keys) {
            val channel = getChannel(channelName) ?: continue

            if (channel.settings.timeoutAfterSeconds < 0)
                continue

            // if enough time elapsed, delete channel
            if (System.currentTimeMillis() - channel.lastMessage > channel.settings.timeoutAfterSeconds * 1000)
                channelsMap.remove(channelName)
        }

        // check player's active channels timeout
        for (player in playersActiveChannels.keys) {
            if (plugin.server.getPlayer(player) == null && playersActiveChannelsTimeoutMap[player]?.let { it < 0 } == true) {
                removeActiveChannels(player)
                playersActiveChannelsTimeoutMap.remove(player)
                continue
            } else {
                playersActiveChannelsTimeoutMap[player] = playersActiveChannelsTimeoutMap[player]?.minus(1) ?: 0
            }
        }
    }

}
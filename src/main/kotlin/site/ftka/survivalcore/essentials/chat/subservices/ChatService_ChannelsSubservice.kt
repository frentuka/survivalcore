package site.ftka.survivalcore.essentials.chat.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.chat.ChatEssential
import site.ftka.survivalcore.essentials.chat.objects.ChatChannel
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

internal class ChatService_ChannelsSubservice(private val service: ChatEssential, private val plugin: MClass) {

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

    fun registerChannel(name: String, isPlayerChannel: Boolean = true, playerUUID: UUID? = null, channelSettings: ChatChannel.ChatChannelSettings = ChatChannel.ChatChannelSettings()): ChatChannel {
        val channel = ChatChannel(service, name)
        channel.settings = channelSettings

        startPurgeDataTimer()

        if (isPlayerChannel && playerUUID != null)
            channelsMap[playerUUID.toString()] = channel
        else
            channelsMap[name] = channel

        return channel
    }

    fun removeChannel(channelName: String) {
        channelsMap.remove(channelName)

        for (player in playersActiveChannels.keys)
            playersActiveChannels[player] = playersActiveChannels[player]!!.minus(channelName)
    }

    fun modifyChannel(channelName: String, modification: (ChatChannel) -> Unit) {
        val channel = channelsMap[channelName] ?: return
        modification(channel)
        channelsMap[channelName] = channel
    }

    // gets

    private fun getElementalChannelSettings(): ChatChannel.ChatChannelSettings {
        val elementalChannelSettings = ChatChannel.ChatChannelSettings()
        elementalChannelSettings.maxStoredChatEntries = 100 // full chat page
        elementalChannelSettings.timeoutAfterSeconds = -1   // no timeout
        return elementalChannelSettings
    }

    fun existsChannel(channelName: String): Boolean =
        channelsMap.containsKey(channelName)

    fun getAllChannels(pretty: Boolean = true, uuidToDelete: UUID? = null): Set<String> {
        if (!pretty)
            return channelsMap.keys

        /*
            channels but pretty:
             - first, global and staff channels
             - then, clan channels
             - then, player channels. if specified, remove uuidToDelete
         */

        val channels = mutableSetOf<String>()
        channels.add(GLOBAL_CHANNEL_NAME)
        channels.add(STAFF_CHANNEL_NAME)

        for (channelName in channelsMap.keys) {
            if (channelName.startsWith(CLAN_CHANNEL_NAME_PREFIX))
                channels.add(channelName)
        }

        for (channelName in channelsMap.keys)
            if (!channelName.startsWith(CLAN_CHANNEL_NAME_PREFIX) && channelName != GLOBAL_CHANNEL_NAME && channelName != STAFF_CHANNEL_NAME && channelName != uuidToDelete?.toString())
                channels.add(channelName)

        return channels
    }

    fun getChannel(name: String): ChatChannel? = channelsMap[name]

    // get or create
    fun getGlobalChannel(): ChatChannel {
        return getChannel(GLOBAL_CHANNEL_NAME) ?:
            registerChannel(GLOBAL_CHANNEL_NAME, false, null, getElementalChannelSettings())
    }

    // get or create
    fun getStaffChannel(): ChatChannel {
        return getChannel(STAFF_CHANNEL_NAME) ?:
            registerChannel(STAFF_CHANNEL_NAME, false, null, getElementalChannelSettings())
    }

    fun getPlayerChannel(uuid: UUID, createIfNotExists: Boolean = true): ChatChannel? {
        val name = plugin.server.getPlayer(uuid)?.uniqueId ?: return null
            return getPlayerChannel(name, createIfNotExists)
    }

    fun getPlayerChannel(playerName: String, createIfNotExists: Boolean = true): ChatChannel? {
        if (channelsMap.containsKey(playerName))
            return channelsMap[playerName]

        val uuid = plugin.server.getPlayer(playerName)?.uniqueId ?: return null

        if (createIfNotExists)
            return registerChannel(playerName, true, uuid)

        return null
    }

    /*
        active channels
     */

    fun getActiveChannels(uuid: UUID): Set<String> {
        return playersActiveChannels[uuid] ?: setOf()
    }

    fun addActiveChannel(uuid: UUID, channel: String) {
        if (!playersActiveChannels.containsKey(uuid))
            playersActiveChannels[uuid] = setOf()

        playersActiveChannels[uuid] = playersActiveChannels[uuid]!!.plus(channel)
    }

    fun removeActiveChannel(uuid: UUID, channel: String) {
        if (!playersActiveChannels.containsKey(uuid))
            return

        playersActiveChannels[uuid] = playersActiveChannels[uuid]!!.minus(channel)
    }

    /*
        data purge timeout
     */

    fun purgePlayer(uuid: UUID) {
        playersActiveChannels.remove(uuid)
    }

    // clock that will do purgeDataCheck every 10 seconds
    private var purgeScheduledFuture: ScheduledFuture<*>? = null
    private var playersActiveChannelsTimeoutMap = mutableMapOf<UUID, Int>()

    private fun startPurgeDataTimer() {
        if (purgeScheduledFuture != null)
            return

        purgeScheduledFuture = plugin.globalScheduler.scheduleAtFixedRate({
            purgeDataCheck()
        }, 10, 10, TimeUnit.SECONDS)
    }

    // will remove data from channels if they dont update in certain amount of time
    private fun purgeDataCheck() {
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
                purgePlayer(player)
                playersActiveChannelsTimeoutMap.remove(player)
                continue
            } else {
                playersActiveChannelsTimeoutMap[player] = playersActiveChannelsTimeoutMap[player]?.minus(1) ?: 0
            }
        }
    }

}
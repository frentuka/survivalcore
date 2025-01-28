package site.ftka.survivalcore.services.playerdata.subservices

import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.PlayerDataService
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*
import java.util.concurrent.TimeUnit

internal class PlayerData_CachingSubservice(private val service: PlayerDataService, private val plugin: MClass) {
    private val logger = service.logger.sub("Caching")

    private val essFwk = plugin.essentialsFwk

    /*
        Saved playerdata will be stored here for a couple minutes (defined by recentPlayerDataSaveTime)
        This service is meant to reduce calls to database
    */

    //                                      <UUID, Death time>
    val playerDataCfg = essFwk.configs.playerdataCfg()

    private val expirationTimes = mutableMapOf<UUID, Long>()
    private val storedPlayerData = mutableMapOf<UUID, PlayerData>()
    private var cachedDataTTL = playerDataCfg.cache.timeToLiveMillis // 30 minutes (in millis)

    init {
        plugin.globalScheduler.scheduleAtFixedRate(
            { cleanupExpiredData() }, 10, playerDataCfg.cache.clockLoopTimeSecs, TimeUnit.SECONDS)
    }

    private fun cleanupExpiredData() {
        val currentTime = System.currentTimeMillis()

        val entriesToRemove = expirationTimes.filter { it.value < currentTime }

        for (uuid in entriesToRemove.keys)
            deleteCachedData(uuid)
    }

    private fun deleteCachedData(uuid: UUID) {
        // Remove entry from expirationTimes
        expirationTimes.remove(uuid)

        // Remove corresponding player data entry
        storedPlayerData.remove(uuid)

        logger.log("Removed from cache ($uuid)")
    }

    /*
                    PUBLIC ZONE
     */

    fun storeLatestPlayerData(playerdata: PlayerData) {
        val currentTime = System.currentTimeMillis()
        val expireTime = currentTime + cachedDataTTL

        expirationTimes[playerdata.uuid] = expireTime
        storedPlayerData[playerdata.uuid] = playerdata

        logger.log("Stored into cache (${playerdata.uuid})")
    }

    fun getCachedPlayerData(uuid: UUID, deleteIt: Boolean = true): PlayerData? {
        val pdata = storedPlayerData[uuid]
        if (deleteIt) deleteCachedData(uuid)

        logger.log("PlayerData was retrieved from cache ($uuid)")

        return pdata
    }

    fun isCached(uuid: UUID) = expirationTimes.containsKey(uuid)
}
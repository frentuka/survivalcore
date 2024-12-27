package site.ftka.survivalcore.services.playerdata

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.playerdata.objects.PlayerData
import java.util.*

class PlayerDataServiceData(private val service: PlayerDataService, private val plugin: MClass) {

    // playerDataMap saves player's playerdata
    // onlinePlayers (UUIDs -> Usernames) is controlled by PlayerDataListener
    private val playerDataMap: MutableMap<UUID, PlayerData> = mutableMapOf()
    private val onlinePlayers: MutableMap<UUID, String> = mutableMapOf()

    fun getPlayerDataMap() =
        playerDataMap

    fun getPlayerDataSet() =
        playerDataMap.values.toSet()

    fun exists(uuid: UUID) =
        playerDataMap.containsKey(uuid)

    fun getPlayerData(uuid: UUID) =
        playerDataMap[uuid]

    fun putPlayerData(uuid: UUID, playerData: PlayerData) =
        playerDataMap.put(uuid, playerData)

    fun removePlayerData(uuid: UUID) =
        playerDataMap.remove(uuid)

    fun getOnlinePlayers() =
        onlinePlayers

    fun putOnlinePlayer(uuid: UUID, username: String) =
        onlinePlayers.put(uuid, username)

    fun removeOnlinePlayer(uuid: UUID) =
        onlinePlayers.remove(uuid)

    fun clearData() {
        playerDataMap.clear()
        onlinePlayers.clear()
    }

    fun clearPlayerDataMap() =
        playerDataMap.clear()

    fun clearOnlinePlayersMap() =
        onlinePlayers.clear()

    private val modificationMutex = Mutex()
    suspend fun makeModification(uuid: UUID, modification: (PlayerData) -> Unit) {
        modificationMutex.withLock {
            if (playerDataMap.containsKey(uuid))
                modification(playerDataMap.get(uuid)!!)
        }
    }

}
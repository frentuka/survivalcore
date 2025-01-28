package site.ftka.survivalcore.essentials.usernameTracker

import com.google.gson.GsonBuilder
import java.util.UUID

internal class UsernameTrackerData {

    private val map = mutableMapOf<UUID, String>()

    fun addElement(uuid: UUID, username: String) {
        map[uuid] = username
    }

    fun getUsername(uuid: UUID): String? {
        return map[uuid]
    }

    fun removeElement(uuid: UUID) {
        map.remove(uuid)
    }

    fun getMap(): Map<UUID, String> {
        return map
    }

    fun contains(uuid: UUID): Boolean {
        return map.containsKey(uuid)
    }

    fun contains(name: String): Boolean {
        return map.containsValue(name)
    }

    fun clear() {
        map.clear()
    }

    fun toJson(): String {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        return gsonPretty.toJson(this)
    }
}
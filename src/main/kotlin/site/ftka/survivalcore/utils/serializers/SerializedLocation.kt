package site.ftka.survivalcore.utils.serializers

import org.bukkit.Bukkit
import org.bukkit.Location

/**
 * Represents a serializable version of a Bukkit Location.
 *
 * @property worldName The name of the world. Defaults to "world".
 * @property coordinates A Triple containing the x, y, and z coordinates. Defaults to (0.5, 63.1, 0.5).
 * @property pitch The pitch of the location. Defaults to 0.0f.
 * @property yaw The yaw of the location. Defaults to 0.0f.
 */
data class SerializedLocation(var worldName: String = "world") {

    private var coordinates: Triple<Double, Double, Double> = Triple(0.5, 63.1, 0.5)

    private var pitch: Float = 0.0f
    private var yaw: Float = 0.0f

    /**
     * Sets the properties of this SerializedLocation from a given Bukkit Location.
     *
     * @param location The Bukkit Location to copy properties from.
     * @return This SerializedLocation instance, allowing for method chaining.
     */
    fun setFrom(location: Location): SerializedLocation {
        worldName = location.world.name
        coordinates = Triple(location.x, location.y, location.z)
        pitch = location.pitch
        yaw = location.yaw
        return this
    }

    /**
     * Converts this SerializedLocation to a Bukkit Location.
     *
     * @return A new Bukkit Location instance with properties set from this SerializedLocation.
     */
    fun asLocation(): Location {
        val world = Bukkit.getWorld(worldName)
        val location = Location(world, coordinates.first, coordinates.second, coordinates.third)
        location.pitch = pitch
        location.yaw = yaw

        return location
    }

}
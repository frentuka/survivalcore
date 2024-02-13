package site.ftka.survivalcore.utils.serializers

import org.bukkit.Bukkit
import org.bukkit.Location

data class SerializedLocation(var worldName: String = "world") {

    var coordinates: Triple<Double, Double, Double>
    = Triple(0.5, 63.1, 0.5)

    var pitch: Float
    = 0.0f
    var yaw: Float
    = 0.0f

    fun setFrom(location: Location): SerializedLocation {
        worldName = location.world.name
        coordinates = Triple(location.x, location.y, location.z)
        pitch = location.pitch
        yaw = location.yaw
        return this
    }

    fun asLocation(): Location {
        val world = Bukkit.getWorld(worldName)
        val location = Location(world, coordinates.first, coordinates.second, coordinates.third)
        location.pitch = pitch
        location.yaw = yaw

        return location
    }

}
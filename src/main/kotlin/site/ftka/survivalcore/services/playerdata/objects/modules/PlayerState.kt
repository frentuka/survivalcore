package site.ftka.survivalcore.services.playerdata.objects.modules

import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.utils.base64Utils
import java.util.UUID
import site.ftka.survivalcore.utils.numericUtils.roundDecimals

data class PlayerState(private val uuid: UUID) {

    /*
        This class is NOT meant to request data when player is online.
        This class is only meant to save player's state in database
        for offline usage.

        It is saved before player unregister and loaded on player register.
        It is NOT real-time data.

        That's why variables doesn't have any direct connection to the physical player.
     */

    var health: Double = 20.0
    var foodLevel: Int = 20
    var saturation: Float = 20f
    var experience: Float = 0f

    var world: String = "world" // Default world the player must be located in
    var location: Triple<Double, Double, Double> = Triple(0.5, 100.1, 0.5) // default coordinates
    var momentum: Triple<Double, Double, Double> = Triple(0.0, 0.0, 0.0) // default velocity
    var fall_distance: Float = 0.0F
    var pitch_yaw: Pair<Float, Float> = Pair(0.0f, 0.0f)

    var inventory: MutableMap<Int, String> = mutableMapOf()
    var enderchest: MutableMap<Int, String> = mutableMapOf()


    /*
        functions
     */


    fun gatherValuesFromPlayer(player: Player) {
        this.health = player.health
        this.foodLevel = player.foodLevel
        this.saturation = player.saturation
        this.experience = player.exp

        // location stuff
        val loc = player.location
        val vel = player.velocity
        this.world = loc.world.name
        this.location = Triple(loc.x, loc.y, loc.z)
        this.momentum = Triple(vel.x, vel.y, vel.z)
        this.fall_distance = player.fallDistance
        this.pitch_yaw = Pair(loc.pitch.roundDecimals(2), loc.yaw.roundDecimals(2))

        // inventory stuff
        this.inventory.clear()
        for (item in player.inventory.withIndex())
            item.value?.let{ this.inventory[item.index] = base64Utils.toBase64(it) }

        this.enderchest.clear()
        for (item in player.enderChest.withIndex())
            item.value?.let{ this.enderchest[item.index] = base64Utils.toBase64(it) }
    }

    fun applyValuesToPlayer(plugin: MClass, player: Player) {
        player.health = this.health
        player.foodLevel = this.foodLevel
        player.saturation = this.saturation;
        player.exp = this.experience

        // inventory stuff
        val pinv = player.inventory
        pinv.clear()
        for (item in this.inventory)
            pinv.setItem(item.key, base64Utils.fromBase64(item.value))

        val pend = player.enderChest
        for (item in this.enderchest)
            pend.setItem(item.key, base64Utils.fromBase64(item.value))

        // location stuff
        val loc = Location(Bukkit.getWorld(this.world), this.location.first, this.location.second, this.location.third)

        plugin.server.scheduler.runTaskLater(plugin, Runnable{
            loc.pitch = this.pitch_yaw.first
            loc.yaw = this.pitch_yaw.second
            player.teleport(loc)
            player.fallDistance = this.fall_distance
            plugin.server.scheduler.runTaskLater(plugin, Runnable{
                player.velocity = Vector(this.momentum.first, this.momentum.second, this.momentum.third)
            }, 1L)
        }, 1L)

    }

}
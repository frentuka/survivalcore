package site.ftka.survivalcore.services.playerdata.objects.modules

import kotlinx.coroutines.*
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.utils.base64Utils
import java.util.UUID
import site.ftka.survivalcore.utils.serializers.SerializedLocation

class PlayerState {

    /*
        This class is NOT meant to request data when player is online.
        This class is only meant to save player's state in database
        for offline usage.

        It is saved before player unregister and loaded on player register.
        It is NOT real-time data.

        That's why variables doesn't have any direct connection to the physical player.
     */

    var isDead: Boolean = true
    var health: Double = 20.0
    var foodLevel: Int = 20
    var saturation: Float = 20f
    var experience: Float = 0f

    var bedLocation: SerializedLocation? = null
    var serializedLocation = SerializedLocation()
    var momentum: Triple<Double, Double, Double> = Triple(0.0, 0.0, 0.0) // default velocity
    var fall_distance: Float = 0.0F

    var inventory: MutableMap<Int, String> = mutableMapOf()
    var enderchest: MutableMap<Int, String> = mutableMapOf()


    /*
        functions
     */


    fun gatherValuesFromPlayer(player: Player) {
        this.isDead = player.isDead
        this.health = player.health
        this.foodLevel = player.foodLevel
        this.saturation = player.saturation
        this.experience = player.exp

        // location stuff

        // could fail, player.bedLocation throws IllegalStateExc if player has never slept
        try { this.bedLocation = SerializedLocation().setFrom(player.bedLocation) }
        catch (_: Exception) {}

        this.serializedLocation = SerializedLocation().setFrom(player.location)
        val vel = player.velocity
        this.momentum = Triple(vel.x, vel.y, vel.z)
        this.fall_distance = player.fallDistance

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
        val toBeAppliedLocation = serializedLocation.asLocation()

        // apply location 1 tick after joining
        if (!this.isDead)
        plugin.server.scheduler.runTaskLater(plugin, Runnable{
            player.teleport(toBeAppliedLocation)
            player.fallDistance = this.fall_distance
            plugin.server.scheduler.runTaskLater(plugin, Runnable{
                player.velocity = Vector(this.momentum.first, this.momentum.second, this.momentum.third)
            }, 1L)
        }, 1L)

    }

}
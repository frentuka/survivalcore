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
    var level: Int = 0
    var gameMode: String = "SURVIVAL"
    var potionEffects: List<SerializedPotionEffect> = listOf()

    data class SerializedPotionEffect(
        val type: String,
        val duration: Int,
        val amplifier: Int,
        val ambient: Boolean,
        val particles: Boolean,
        val icon: Boolean
    )

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
        this.level = player.level
        this.gameMode = player.gameMode.name
        
        this.potionEffects = player.activePotionEffects.map {
            SerializedPotionEffect(
                type = it.type.name,
                duration = it.duration,
                amplifier = it.amplifier,
                ambient = it.isAmbient,
                particles = it.hasParticles(),
                icon = it.hasIcon()
            )
        }

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
        player.saturation = this.saturation
        player.exp = this.experience
        player.level = this.level
        
        try {
            player.gameMode = org.bukkit.GameMode.valueOf(this.gameMode)
        } catch (_: Exception) {
            player.gameMode = org.bukkit.GameMode.SURVIVAL
        }

        // restore potion effects
        for (effect in player.activePotionEffects) {
            player.removePotionEffect(effect.type)
        }
        for (serialized in this.potionEffects) {
            @Suppress("DEPRECATION")
            val type = org.bukkit.potion.PotionEffectType.getByName(serialized.type) ?: continue
            val effect = org.bukkit.potion.PotionEffect(
                type,
                serialized.duration,
                serialized.amplifier,
                serialized.ambient,
                serialized.particles,
                serialized.icon
            )
            player.addPotionEffect(effect)
        }

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
package site.ftka.survivalcore.services.playerdata.objects.modules

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import site.ftka.survivalcore.utils.base64Utils
import site.ftka.survivalcore.utils.dataclasses.DoublesVector
import java.util.UUID
import site.ftka.survivalcore.utils.numericUtils.roundDecimals

data class PlayerState(private val uuid: UUID) {

    var health: Double = 20.0
    var foodLevel: Int = 20
    var saturation: Float = 20f
    var experience: Float = 0f

    var world: String = "world" // Default world the player must be located in
    var location: DoublesVector = DoublesVector(0.5, 100.1, 0.5) // default coordinates
    var momentum: DoublesVector = DoublesVector(0.0, 0.0, 0.0) // default velocity
    var pitch_yaw: Pair<Float, Float> = Pair(0.0f, 0.0f)

    var inventory: MutableMap<Int, String> = mutableMapOf()
    var enderchest: MutableMap<Int, String> = mutableMapOf()


    /*
        functions
     */


    fun replaceValuesFromPlayer(player: Player) {
        this.health = player.health
        this.foodLevel = player.foodLevel
        this.saturation = player.saturation
        this.experience = player.exp

        // location stuff
        val loc = player.location
        val vel = player.velocity
        this.world = loc.world.name
        this.location = DoublesVector(loc.x.roundDecimals(2), loc.y.roundDecimals(2), loc.z.roundDecimals(2))
        this.momentum = DoublesVector(vel.x.roundDecimals(2), vel.y.roundDecimals(2), vel.z.roundDecimals(2))
        this.pitch_yaw = Pair(loc.pitch.roundDecimals(2), loc.yaw.roundDecimals(2))

        // inventory stuff
        for (item in player.inventory.withIndex())
            item.value?.let{ this.inventory[item.index] = base64Utils.toBase64(it) }

        for (item in player.enderChest.withIndex())
            item.value?.let{ this.enderchest[item.index] = base64Utils.toBase64(it) }
    }

    fun applyValuesToPlayer(player: Player) {
        player.health = this.health
        player.foodLevel = this.foodLevel
        player.saturation = this.saturation
        player.exp = this.experience

        // location stuff
        val loc = Location(Bukkit.getWorld(this.world), this.location.x, this.location.y, this.location.z)
        loc.pitch = this.pitch_yaw.first
        loc.yaw = this.pitch_yaw.second
        player.velocity = Vector(this.momentum.x, this.momentum.y, this.momentum.z)

        // inventory stuff
        val pinv = player.inventory
        pinv.clear()
        for (item in this.inventory)
            pinv.setItem(item.key, base64Utils.fromBase64(item.value))

        val pend = player.enderChest
        for (item in this.enderchest)
            pend.setItem(item.key, base64Utils.fromBase64(item.value))
    }

}
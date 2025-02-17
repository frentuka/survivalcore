package site.ftka.survivalcore.essentials.bossbar.objects

import net.kyori.adventure.bossbar.BossBar
import org.bukkit.Location

/**
 * Represents a bossbar that is localized to a specific location
 *
 * @param barOrigin The location of the bossbar
 * @param bossbar The bossbar itself
 */
class PropBossBar(val bossbar: BossBar, var barOrigin: Location? = null) {

    /*
        Shortcuts for the bossbar's properties
     */

    var name
        get() = bossbar.name()
        set(value) { bossbar.name(value) }

    var progress
        get() = bossbar.progress()
        set(value) { bossbar.progress(value) }

    var color
        get() = bossbar.color()
        set(value) { bossbar.color(value) }

    var overlay
        get() = bossbar.overlay()
        set(value) { bossbar.overlay(value) }

    var flags
        get() = bossbar.flags()
        set(value) { bossbar.flags(value) }

    /**
     * Returns the distance between the bossbar's location and the given location
     *
     * @param location The location to calculate the distance to
     */
    fun distance(location: Location)
        = this.barOrigin?.distance(location)

    /**
     * Returns a vector from the bossbar's location to the given location
     * To be used for compass-like effects
     *
     * @param location The location to calculate the vector to
     */
    fun relativeLocationVector(location: Location)
        = this.barOrigin?.toVector()?.subtract(location.toVector())

    /**
     * Returns the angle between the bossbar's direction and the given location
     *
     * @param location The location to calculate the angle to
     * @return The angle in radians
     */
    fun relativeAngleRadians(location: Location)
        = this.barOrigin?.direction?.angle(location.direction)

    /**
     * Returns the angle between the bossbar's direction and the given location
     *
     * @param location The location to calculate the angle to
     * @return The angle in degrees
     */
    fun relativeAngleDegrees(location: Location): Double? {
        val radians = this.relativeAngleRadians(location) ?: return null
        return Math.toDegrees(radians.toDouble())
    }

}
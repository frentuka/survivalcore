package site.ftka.survivalcore.services.worldboard.objects

import org.bukkit.inventory.ItemStack
import org.joml.Vector3f

/**
 * Represents a single inline icon for a WorldBoard.
 *
 * @param item The ItemStack to be displayed.
 * @param translation The 3D relative offset of the icon from the center of the TextDisplay.
 * @param scaleMultiplier The multiplier applied to the base scale of the WorldBoard.
 */
data class WorldBoardIcon(
    val item: ItemStack,
    val translation: Vector3f = Vector3f(0f, 0f, -0.03f),
    val scaleMultiplier: Float = 0.25f
)

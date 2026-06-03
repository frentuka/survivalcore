package site.ftka.survivalcore.services.worldboard.objects

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.inventory.ItemStack
import org.joml.Vector3f
import site.ftka.survivalcore.utils.MinecraftFontWidthCalculator

/**
 * A builder that facilitates perfectly aligning ItemDisplays inline with TextDisplays.
 * TextDisplays center all lines relative to each other by default.
 */
class WorldBoardTextLayout {
    private val lines = mutableListOf<LineData>()
    private var currentLine = LineData()

    init {
        lines.add(currentLine)
    }

    data class LineData(
        var component: Component = Component.empty(),
        var plainText: String = "",
        val icons: MutableList<IconRequest> = mutableListOf()
    )

    data class IconRequest(
        val item: ItemStack,
        val charIndexInLine: Int,
        val spacesWidth: Int,
        val verticalOffset: Float,
        val scaleMultiplier: Float,
        val zOffset: Float
    )

    /**
     * Appends a component to the current line.
     * Use `newLine()` to create new lines instead of passing `\n` inside components.
     */
    fun append(comp: Component): WorldBoardTextLayout {
        currentLine.component = currentLine.component.append(comp)
        currentLine.plainText += PlainTextComponentSerializer.plainText().serialize(comp).replace("\n", "")
        return this
    }

    /**
     * Appends a literal string to the current line.
     */
    fun append(text: String): WorldBoardTextLayout {
        return append(Component.text(text))
    }

    /**
     * Starts a new line.
     */
    fun newLine(): WorldBoardTextLayout {
        currentLine = LineData()
        lines.add(currentLine)
        return this
    }

    /**
     * Injects an icon into the flow of the text. 
     * Behind the scenes, it adds transparent spaces to make room for the icon,
     * and calculates the exact X coordinate where the icon should be spawned.
     * 
     * @param item The item to display.
     * @param spaces The number of spaces to insert as padding (default 3 is usually good for a 0.23f scaled item).
     * @param verticalOffset The vertical offset of the item relative to the TextDisplay center.
     * @param scaleMultiplier The scale of the item.
     */
    fun appendIcon(
        item: ItemStack,
        spaces: Int = 3,
        verticalOffset: Float = -0.16f,
        scaleMultiplier: Float = 0.23f,
        zOffset: Float = -0.03f
    ): WorldBoardTextLayout {
        val spaceStr = " ".repeat(spaces)
        val spacesWidth = MinecraftFontWidthCalculator.getStringWidth(spaceStr)
        
        val req = IconRequest(item, currentLine.plainText.length, spacesWidth, verticalOffset, scaleMultiplier, zOffset)
        currentLine.icons.add(req)
        
        currentLine.component = currentLine.component.append(Component.text(spaceStr))
        currentLine.plainText += spaceStr
        return this
    }

    /**
     * Builds the final Component to be set on the TextDisplay.
     */
    fun buildComponent(): Component {
        var finalComp = Component.empty()
        for ((i, line) in lines.withIndex()) {
            finalComp = finalComp.append(line.component)
            if (i < lines.size - 1) {
                finalComp = finalComp.append(Component.text("\n"))
            }
        }
        return finalComp
    }

    /**
     * Calculates and returns the exact 3D offsets for each inline icon requested.
     * @param frame Optional frame that will be applied to the board. Required to calculate exact Y offsets since frames add lines.
     */
    fun buildIcons(frame: WorldBoardFrame? = null): List<WorldBoardIcon> {
        val result = mutableListOf<WorldBoardIcon>()
        
        // A TextDisplay's X-axis translation unit is roughly 1 pixel = 0.025 blocks.
        // A TextDisplay's line height is exactly 10 pixels = 0.25 blocks.
        val pixelToBlockRatio = 0.025f
        val lineHeightBlocks = 0.25f
        
        // If a frame is used, it adds padding lines. 
        // For ROUNDED, it adds 2 lines at the top and 2 at the bottom.
        val topFrameLines = if (frame != null) 2 else 0
        val bottomFrameLines = if (frame != null) 2 else 0
        val totalLines = lines.size + topFrameLines + bottomFrameLines

        for ((lineIndex, line) in lines.withIndex()) {
            val lineWidthPixels = MinecraftFontWidthCalculator.getStringWidth(line.plainText)
            
            // In TextDisplay local space (facing it), +X is RIGHT and -X is LEFT.
            // The text is rendered from LEFT to RIGHT.
            // This means the start of the text is on the NEGATIVE X side.
            val lineStartX = -(lineWidthPixels / 2f) * pixelToBlockRatio

            for (icon in line.icons) {
                // Calculate the width of the text *before* the injected spaces
                val textBefore = line.plainText.substring(0, icon.charIndexInLine)
                val widthBeforePixels = MinecraftFontWidthCalculator.getStringWidth(textBefore)
                
                // The spaces we added have width `icon.spacesWidth`.
                // There is a 1-pixel gap between the last character of `textBefore` and the first space.
                val startPixelOfSpaces = widthBeforePixels + 1f
                val centerPixelOfSpaces = startPixelOfSpaces + (icon.spacesWidth / 2f)
                
                // Since text goes from -X to +X, we add the distance from start
                val iconXBlocks = lineStartX + (centerPixelOfSpaces * pixelToBlockRatio)

                // TextDisplay vertical origin is at the BOTTOM CENTER of the entire text block.
                // Each line is 0.25 blocks tall. The first line (index 0) is at the top.
                // The absolute line index in the final rendered text is:
                val absoluteLineIndex = lineIndex + topFrameLines
                
                // Y offset from bottom = (totalLines - 1 - absoluteLineIndex) * lineHeightBlocks
                // We add the user-provided verticalOffset for fine-tuning.
                val calculatedY = ((totalLines - 1 - absoluteLineIndex) * lineHeightBlocks) + icon.verticalOffset

                result.add(
                    WorldBoardIcon(
                        item = icon.item,
                        translation = Vector3f(iconXBlocks, calculatedY, icon.zOffset),
                        scaleMultiplier = icon.scaleMultiplier
                    )
                )
            }
        }
        
        return result
    }
}

package site.ftka.survivalcore.services.worldboard.objects

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * Provides aesthetic unicode framing options for WorldBoards.
 * Includes built-in smooth gradients and dynamic component wrapping.
 */
enum class WorldBoardFrame(
    val topLeft: String,
    val topRight: String,
    val bottomLeft: String,
    val bottomRight: String,
    val horizontal: String,
    val defaultGradient: String
) {
    ROUNDED("╭", "╮", "╰", "╯", "─", "<gradient:#ff0055:#ff9900>");

    /**
     * Wraps the provided Component within a dynamically generated top and bottom horizontal frame
     * matching the length of the longest text line in the board contents.
     */
    fun wrap(text: Component, gradient: String = defaultGradient): Component {
        val mm = MiniMessage.miniMessage()
        
        // 1. Measure the plain-text length of the longest line in the board component
        val plainText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(text)
        val lines = plainText.split("\n")
        val maxLen = lines.maxOfOrNull { it.length } ?: 10
        
        // 2. Scale the border length down to compensate for Unicode characters
        // being significantly wider in Minecraft's default font than alphanumeric characters.
        // A factor of 0.65 matches standard character widths perfectly in pixel space.
        val borderLen = maxOf((maxLen * 0.65).toInt(), 4)
        
        // 3. Dynamically construct the horizontal border lines
        val dynamicTop = topLeft + horizontal.repeat(borderLen) + topRight
        val dynamicBottom = bottomLeft + horizontal.repeat(borderLen) + bottomRight
        
        // 4. Wrap with 1-space-height padding (double newlines) between borders and content
        val topComp = mm.deserialize("$gradient$dynamicTop</gradient>\n\n")
        val bottomComp = mm.deserialize("\n\n$gradient$dynamicBottom</gradient>")
        return topComp.append(text).append(bottomComp)
    }
}

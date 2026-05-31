package site.ftka.survivalcore.utils

/**
 * Utility to calculate the exact pixel width of text rendered by Minecraft's default font.
 * Crucial for precisely positioning displays (like ItemDisplays) inline with TextDisplays.
 */
object MinecraftFontWidthCalculator {
    private val charWidths = mapOf(
        ' ' to 3,
        '!' to 1,
        '"' to 3,
        '\'' to 1,
        '(' to 3,
        ')' to 3,
        '*' to 3,
        ',' to 1,
        '.' to 1,
        ':' to 1,
        ';' to 1,
        '<' to 4,
        '>' to 4,
        '@' to 6,
        'I' to 3,
        '[' to 3,
        ']' to 3,
        '`' to 2,
        'f' to 4,
        'i' to 1,
        'k' to 4,
        'l' to 2,
        't' to 3,
        '{' to 3,
        '|' to 1,
        '}' to 3,
        '~' to 6
    )

    fun getCharWidth(c: Char): Int {
        if (c == '\n') return 0
        if (c.code == 167) return -1 // section sign §
        return charWidths[c] ?: 5
    }

    /**
     * Calculates the width in pixels of the given string, accounting for 
     * character widths and the 1-pixel spacing between characters.
     */
    fun getStringWidth(str: String): Int {
        var width = 0
        var i = 0
        while (i < str.length) {
            val c = str[i]
            if (c == '§') {
                i += 2 // Skip formatting codes
                continue
            }
            width += getCharWidth(c) + 1 // +1 for spacing between characters
            i++
        }
        if (width > 0) width -= 1 // No trailing spacing after the last character
        return width
    }
}

package site.ftka.survivalcore.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import java.util.*
import java.util.function.Consumer

object textUtils {

    fun asPlainText(arguments: Array<String>?): String {
        val sb = StringBuilder()
        Arrays.stream(arguments).forEach { arg: String? -> sb.append(arg).append(" ") }
        return sb.substring(0, sb.length - 1)
    }

    // For testing purposes only
    fun randomTextGenerator(length: Int): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val sb = StringBuilder()
        val random = Random()
        for (i in 0 until length) {
            sb.append(alphabet[random.nextInt(alphabet.length)])
        }
        return sb.toString()
    }

    fun hexComponent(text: String, hexColor: String) =
        Component.text(text).color(TextColor.fromHexString(hexColor))
}
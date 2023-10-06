package site.ftka.survivalcore.utils

import net.kyori.adventure.text.Component
import java.util.*
import java.util.function.Consumer

object textUtils {
    fun col(text: String): Component {
        val text_char = text.toCharArray()
        for (i in 0 until text_char.size - 1) {
            if (text_char[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(text_char[i + 1]) > -1) {
                text_char[i] = '\u00A7'
                text_char[i + 1] = text_char[i + 1].lowercaseChar()
            }
        }
        return Component.text(String(text_char))
    }

    fun col(text_list: List<String>): List<Component> {
        val list: MutableList<Component> = ArrayList()
        text_list.forEach(Consumer { text: String -> list.add(col(text)) })
        return list
    }

    fun col(text_list: Array<String>?): List<Component> {
        val list: MutableList<Component> = ArrayList()
        Arrays.stream(text_list).forEach { text: String -> list.add(col(text)) }
        return list
    }

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
}
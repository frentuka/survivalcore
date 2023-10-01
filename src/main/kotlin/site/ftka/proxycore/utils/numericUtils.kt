package site.ftka.proxycore.utils

import java.util.concurrent.ThreadLocalRandom

object numericUtils {
    fun getRandomInt(minInclusive: Int, maxInclusive: Int): Int {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1)
    }

    fun isIntegerParseable(textToVerify: String): Boolean {
        return textToVerify.chars().allMatch { codePoint: Int -> Character.isDigit(codePoint) }
    }
}
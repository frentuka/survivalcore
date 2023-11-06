package site.ftka.survivalcore.utils

import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow

object numericUtils {
    fun getRandomInt(minInclusive: Int, maxInclusive: Int): Int {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1)
    }

    fun isIntegerParseable(textToVerify: String): Boolean {
        return textToVerify.chars().allMatch { codePoint: Int -> Character.isDigit(codePoint) }
    }

    fun Double.roundDecimals(decimalPlaces: Int): Double {
        val factor = 10.0.pow(decimalPlaces.toDouble())
        return Math.round(this * factor) / factor
    }

    fun Float.roundDecimals(decimalPlaces: Int): Float {
        val factor = 10.0.pow(decimalPlaces.toDouble()).toFloat()
        return Math.round(this * factor) / factor
    }
}
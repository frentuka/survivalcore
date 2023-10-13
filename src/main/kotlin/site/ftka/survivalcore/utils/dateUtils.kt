package site.ftka.survivalcore.utils

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


object dateUtils {

    fun timeFormat(date: Long, pattern: String): String {
        // Convert milliseconds to LocalDateTime
        val instant = Instant.ofEpochMilli(date)
        val localDateTime = instant.atOffset(ZoneOffset.UTC).toLocalDateTime()

        // Define a custom date-time format
        val formatter = DateTimeFormatter.ofPattern(pattern)

        // Format the LocalDateTime to a string
        return localDateTime.format(formatter)
    }

}
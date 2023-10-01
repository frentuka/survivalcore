package site.ftka.proxycore.utils

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


object dateUtils {

    fun format_forFiles(date: Long): String {
        // Convert milliseconds to LocalDateTime
        val instant = Instant.ofEpochMilli(date)
        val localDateTime = instant.atOffset(ZoneOffset.UTC).toLocalDateTime()

        // Define a custom date-time format
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss")

        // Format the LocalDateTime to a string
        return localDateTime.format(formatter)
    }

}
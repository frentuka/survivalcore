package site.ftka.survivalcore.utils

object objectsSizeUtils {

    fun estimateStringSize(inputString: String): Long {
        // Estimate the size based on the string's character encoding (UTF-16 in this case).
        val charSize = 2 // Size of a Char in bytes in UTF-16 encoding.

        // Calculate the size of the string data (characters).
        val dataSize = inputString.length.toLong() * charSize

        // Estimate any additional overhead for the string object.
        val overhead = 12

        // Calculate the total size.
        val totalSize = dataSize + overhead

        return totalSize
    }

}
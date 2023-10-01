package site.ftka.proxycore.utils

import java.io.File

object jsonFileUtils {

    fun saveJson(location: String, filename: String, data: String) {
        try {
            val folders = File(location)
            folders.mkdirs()
            val file = File("$location\\$filename")
            file.createNewFile()
            file.writeText(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readFile(location: String, fileName: String): String? {
        val theFile = File(location + "\\$fileName")
        if (!theFile.exists()) return null

        return try {
            theFile.readText()
        } catch (e: Exception) {
            null
        }
    }

    // wont read files whose names start with _
    fun readAllJson(location: String): List<String> {
        val folderFile = File(location)
        if (!folderFile.exists()) {
            return listOf()
        }

        val readsList = mutableListOf<String>()

        folderFile.listFiles()?.forEach {
            // is it a valid group file filename?
            val filename = it.name
            val fileresult = readFile(location, filename)
            if (fileresult != null) if (!filename.startsWith("_") && filename.endsWith(".json")) readsList.add(fileresult)
        }

        return readsList
    }

}
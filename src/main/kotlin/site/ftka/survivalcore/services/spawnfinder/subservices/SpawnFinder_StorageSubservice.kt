package site.ftka.survivalcore.services.spawnfinder.subservices

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.spawnfinder.SpawnFinderService
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import java.io.File

class SpawnFinder_StorageSubservice(private val service: SpawnFinderService, private val plugin: MClass) {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File(plugin.dataFolder, "valid_spawns.json")

    fun load() {
        if (!file.exists()) {
            file.createNewFile()
            file.writeText("[]")
            return
        }
        
        try {
            val type = object : TypeToken<List<String>>() {}.type
            val rawList: List<String>? = gson.fromJson(file.readText(), type)
            rawList?.forEach { str ->
                val split = str.split(",")
                if (split.size == 2) {
                    val x = split[0].toIntOrNull()
                    val z = split[1].toIntOrNull()
                    if (x != null && z != null) {
                        service.validSpawns.add(Pair(x, z))
                    }
                }
            }
            service.logger.log("Loaded ${service.validSpawns.size} valid spawn chunks.", LogLevel.LOW)
        } catch (e: Exception) {
            service.logger.log("Failed to load valid spawns: ${e.message}", LogLevel.LOW)
            e.printStackTrace()
        }
    }

    fun save() {
        try {
            val stringList = service.validSpawns.map { "${it.first},${it.second}" }
            file.writeText(gson.toJson(stringList))
        } catch (e: Exception) {
            service.logger.log("Failed to save valid spawns: ${e.message}", LogLevel.LOW)
        }
    }

    fun saveAsync() {
        java.util.concurrent.CompletableFuture.runAsync {
            save()
        }
    }
}

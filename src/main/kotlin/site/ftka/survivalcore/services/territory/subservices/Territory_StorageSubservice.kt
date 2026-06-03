package site.ftka.survivalcore.services.territory.subservices

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.territory.TerritoryService
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import java.io.File
import java.util.UUID

class Territory_StorageSubservice(private val service: TerritoryService, private val plugin: MClass) {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File(plugin.dataFolder, "territory_claims.json")

    fun load() {
        if (!file.exists()) {
            file.createNewFile()
            file.writeText("{}")
            return
        }
        
        try {
            val type = object : TypeToken<Map<String, String>>() {}.type
            val rawMap: Map<String, String>? = gson.fromJson(file.readText(), type)
            rawMap?.forEach { (k, v) ->
                val split = k.split(",")
                if (split.size == 2) {
                    val x = split[0].toIntOrNull()
                    val z = split[1].toIntOrNull()
                    if (x != null && z != null) {
                        service.claims[Pair(x, z)] = java.util.UUID.fromString(v)
                    }
                }
            }
            service.logger.log("Loaded ${service.claims.size} territory claims.", LogLevel.LOW)
        } catch (e: Exception) {
            service.logger.log("Failed to load territory claims: ${e.message}", LogLevel.LOW)
            e.printStackTrace()
        }
    }

    fun save() {
        try {
            val stringMap = mutableMapOf<String, String>()
            service.claims.forEach { (k, v) ->
                stringMap["${k.first},${k.second}"] = v.toString()
            }
            file.writeText(gson.toJson(stringMap))
        } catch (e: Exception) {
            service.logger.log("Failed to save territory claims: ${e.message}", LogLevel.LOW)
        }
    }

    fun saveAsync() {
        java.util.concurrent.CompletableFuture.runAsync {
            save()
        }
    }
}

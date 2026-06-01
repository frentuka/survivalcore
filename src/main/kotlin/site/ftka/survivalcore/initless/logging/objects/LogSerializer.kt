package site.ftka.survivalcore.initless.logging.objects

import com.google.gson.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import site.ftka.survivalcore.initless.logging.LoggingInitless.LogLevel
import java.lang.reflect.Type

internal class LogSerializer : JsonSerializer<Log>, JsonDeserializer<Log> {
    override fun serialize(src: Log, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        obj.addProperty("color", src.color.toString())
        
        val componentJsonStr = GsonComponentSerializer.gson().serialize(src.text)
        obj.add("text", JsonParser.parseString(componentJsonStr))
        
        obj.addProperty("level", src.level.name)
        obj.addProperty("timestamp", src.timestamp)
        return obj
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Log {
        val obj = json.asJsonObject
        
        val colorStr = obj.get("color")?.asString ?: "yellow"
        val color = NamedTextColor.NAMES.value(colorStr) ?: NamedTextColor.YELLOW
        
        val textElement = obj.get("text")
        val text = if (textElement != null) {
            try {
                GsonComponentSerializer.gson().deserialize(textElement.toString())
            } catch (e: Exception) {
                Component.text("[Error: Invalid Log Format]").color(NamedTextColor.RED)
            }
        } else {
            Component.empty()
        }
        
        val levelStr = obj.get("level")?.asString ?: "NORMAL"
        val level = try { LogLevel.valueOf(levelStr) } catch(e: Exception) { LogLevel.NORMAL }
        
        val timestamp = obj.get("timestamp")?.asLong ?: System.currentTimeMillis()
        
        return Log(color, text, level, timestamp)
    }
}

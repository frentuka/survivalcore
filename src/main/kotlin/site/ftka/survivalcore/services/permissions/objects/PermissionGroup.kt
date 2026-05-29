package site.ftka.survivalcore.services.permissions.objects

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.kyori.adventure.text.format.NamedTextColor
import java.util.UUID

class PermissionGroup {

    val uuid: UUID = UUID.randomUUID()
    var name = "NONE"
    var displayName = "NONE"
    var tag = "NONE"
    var category = GroupCategory.normal
    var primaryColor = NamedTextColor.GRAY.toString() //         to get: NamedTextColor.NAMES.value(primaryColor)
    var secondaryColor = NamedTextColor.DARK_GRAY.toString() //  to get: NamedTextColor.NAMES.value(secondaryColor)
    var perms = setOf<String>()
    var inheritances = setOf<UUID>()

    enum class GroupCategory { normal, special, staff }

    fun toJson(): String {
        return gsonPretty.toJson(this)
    }

    companion object {
        private val gsonPretty: Gson = GsonBuilder().setPrettyPrinting().create()
    }
}

package site.ftka.survivalcore.services.permissions.objects

import com.google.gson.GsonBuilder
import net.kyori.adventure.text.format.NamedTextColor
import java.util.UUID

data class PermissionGroup(val uuid: UUID = UUID.randomUUID()) {

    var name = "NONE"
    var tag = "NONE"
    var category = GroupCategory.normal
    var primaryColor = NamedTextColor.GRAY.toString() //         to get: NamedTextColor.NAMES.value(primaryColor)
    var secondaryColor = NamedTextColor.DARK_GRAY.toString() //  to get: NamedTextColor.NAMES.value(secondaryColor)
    var perms = setOf<String>()
    var inheritances = setOf<UUID>()

    enum class GroupCategory { normal, special, staff }

    fun toJson(): String {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        return gsonPretty.toJson(this)
    }
}

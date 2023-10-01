package site.ftka.proxycore.services.permissions.objects

import com.google.gson.GsonBuilder

class PermissionGroup {

    constructor(id: Int) {
        this.id = id
    }

    constructor(id: Int, name: String)

    var id: Int = -666
    var name : String = "DEFAULT"
    var tag: String = "DEF"
    var category: GroupCategory = GroupCategory.normal
    var primaryColor: Char = '7'
    var secondaryColor: Char = '8'
    var perms = mutableListOf<String>()
    var inheritances = mutableListOf<Int>()

    fun toJson(): String {
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        return gsonPretty.toJson(this)
    }

    enum class GroupCategory{
        normal, special, staff
    }
}

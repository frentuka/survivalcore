package site.ftka.survivalcore.services.language

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.language.objects.LanguagePack
import java.util.UUID

class LanguageAPI(private val svc: LanguageService, private val plugin: MClass) {

    fun playerLanguagePack(uuid: UUID): LanguagePack {
        val playerLanguagePackName = svc.data.playerLangMap[uuid]
        return svc.data.langMap[playerLanguagePackName] ?: svc.defaultLanguagePack
    }

    fun permissionGroup_name(uuid: UUID): Component {
        val group = plugin.servicesFwk.permissions.api.getGroup(uuid) ?: return Component.text("")
        return Component.text(group.displayName).color(NamedTextColor.NAMES.keyToValue().get(group.primaryColor))
    }

    fun permissionGroup_chatPrefix(uuid: UUID): Component {
        val group = plugin.servicesFwk.permissions.api.getGroup(uuid) ?: return Component.text("")
        return permissionGroup_name(uuid).append(Component.text(" ✧ ")
            .color(NamedTextColor.NAMES.keyToValue().get(group.secondaryColor)))
    }

    fun  player_chatMessagePrefix(uuid: UUID): Component {
        val playerGroup = plugin.servicesFwk.permissions.api.player_getDisplayGroup(uuid, true).get()
        val playerName = plugin.essentialsFwk.usernameTracker.getName(uuid)

        playerGroup ?: return Component.text("$playerName").color(NamedTextColor.YELLOW)

        return permissionGroup_chatPrefix(playerGroup).append(Component.text("$playerName")
            .color(NamedTextColor.YELLOW))
    }

}
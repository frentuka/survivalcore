package site.ftka.survivalcore.services.playerdata.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import site.ftka.survivalcore.essentials.proprietaryEvents.objects.PropEvent

class PlayerDataRestartEvent: PropEvent {

    override val async = false
    override var cancelled = false

}
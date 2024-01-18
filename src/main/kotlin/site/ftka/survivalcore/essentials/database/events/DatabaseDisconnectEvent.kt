package site.ftka.survivalcore.essentials.database.events

import site.ftka.survivalcore.essentials.proprietaryEvents.objects.PropEvent

class DatabaseDisconnectEvent(): PropEvent {
    override val name = "DatabaseDisconnectEvent"
    override val async = false
    override var cancelled = false
}
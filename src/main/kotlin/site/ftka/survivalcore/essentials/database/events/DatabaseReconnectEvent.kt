package site.ftka.survivalcore.essentials.database.events

import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropEvent

class DatabaseReconnectEvent(): PropEvent {
    override val name = "DatabaseReconnectEvent"
    override val async = false
    override var cancelled = false

}
package site.ftka.survivalcore.essentials.database.events

import site.ftka.survivalcore.essentials.proprietaryEvents.objects.PropEvent

class DatabaseReconnectEvent(): PropEvent {

    override val async = false
    override var cancelled = false

}
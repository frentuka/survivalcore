package site.ftka.survivalcore.services.playerdata.events

import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropEvent

class PlayerDataInitEvent: PropEvent {
    override val name = "PlayerDataInitEvent"
    override val async = false
    override var cancelled = false

}
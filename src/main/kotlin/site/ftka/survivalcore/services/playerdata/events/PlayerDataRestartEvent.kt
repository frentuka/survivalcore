package site.ftka.survivalcore.services.playerdata.events

import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropEvent

class PlayerDataRestartEvent: PropEvent {
    override val name = "PlayerDataRestartEvent"
    override val async = false
    override var cancelled = false

}
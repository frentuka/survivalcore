package site.ftka.survivalcore.services.playerdata.events

import site.ftka.survivalcore.essentials.proprietaryEvents.objects.PropEvent

class PlayerDataInitEvent: PropEvent {

    override val async = false
    override var cancelled = false

}
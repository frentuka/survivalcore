package site.ftka.survivalcore.services.inventorygui.events

import site.ftka.survivalcore.initless.proprietaryEvents.objects.PropEvent

class InventoryGUIRestartEvent: PropEvent {

    override val name = "InventoryGUIRestartEvent"
    override val async = false
    override var cancelled = false

}
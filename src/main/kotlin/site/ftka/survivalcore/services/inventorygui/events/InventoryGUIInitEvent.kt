package site.ftka.survivalcore.services.inventorygui.events

import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropEvent

class InventoryGUIInitEvent: PropEvent {

    override val name = "InventoryGUIInitEvent"
    override val async = false
    override var cancelled = false

}
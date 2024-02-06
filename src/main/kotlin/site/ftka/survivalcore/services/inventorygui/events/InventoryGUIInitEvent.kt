package site.ftka.survivalcore.services.inventorygui.events

import site.ftka.survivalcore.essentials.proprietaryEvents.objects.PropEvent

class InventoryGUIInitEvent: PropEvent {

    override val name = "InventoryGUIInitEvent"
    override val async = false
    override var cancelled = false

}
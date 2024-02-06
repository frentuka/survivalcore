package site.ftka.survivalcore.apps.InventoryGUITester

import site.ftka.survivalcore.MClass

class invguitester(plugin: MClass) {

    val invgowner = invguitesterowner()
    init {
        plugin.initListener(invguitesterlistener(this, plugin))
    }

}
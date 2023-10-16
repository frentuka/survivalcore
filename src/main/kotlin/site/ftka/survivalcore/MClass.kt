package site.ftka.survivalcore

import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import site.ftka.survivalcore.essentials.logging.LoggingEssential
import site.ftka.survivalcore.services.ServicesCore

class MClass: JavaPlugin() {

    // Initialize essentials
    val loggingEssential = LoggingEssential(this)

    // Initialize services
    val services: ServicesCore = ServicesCore(this)

    override fun onEnable() {
        services.initAll()
        initListeners()
    }

    override fun onDisable() {
    }

    private val listenerList = mutableListOf<Listener>()
    fun initListener(listener: Listener) {
        if (listenersAlreadyInitialized) server.pluginManager.registerEvents(listener, this)
        else listenerList.add(listener)
    }

    private var listenersAlreadyInitialized = false
    private fun initListeners() {
        listenersAlreadyInitialized = true

        for (listener in listenerList) {
            server.pluginManager.registerEvents(listener, this)
            listenerList.remove(listener)
        }
    }

}
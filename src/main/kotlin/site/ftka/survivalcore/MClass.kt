package site.ftka.survivalcore

import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import site.ftka.survivalcore.essentials.database.DatabaseEssential
import site.ftka.survivalcore.essentials.logging.LoggingEssential
import site.ftka.survivalcore.services.ServicesCore

class MClass: JavaPlugin() {

    // Initialize essentials
    val loggingEssential = LoggingEssential(this)
    val dbEssential = DatabaseEssential(this)

    // Initialize services
    val servicesCore = ServicesCore(this)

    override fun onEnable() {
        dbEssential.init()

        servicesCore.initAll()

        initListeners()
    }

    override fun onDisable() {
        dbEssential.disconnect()
    }

    private val listenerList = mutableListOf<Listener>()
    fun initListener(listener: Listener) {
        if (listenersAlreadyInitialized) server.pluginManager.registerEvents(listener, this)
        else listenerList.add(listener)
    }

    private var listenersAlreadyInitialized = false
    private fun initListeners() {
        listenersAlreadyInitialized = true

        for (listener in listenerList.toList()) {
            println("Initializing listener ${listener.toString().split(".").last()}")
            server.pluginManager.registerEvents(listener, this)
            listenerList.remove(listener)
        }
    }

}
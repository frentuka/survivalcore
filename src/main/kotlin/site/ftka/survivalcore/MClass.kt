package site.ftka.survivalcore

import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import site.ftka.survivalcore.apps.InventoryGUITester.invguitester
import site.ftka.survivalcore.essentials.configs.ConfigsEssential
import site.ftka.survivalcore.essentials.database.DatabaseEssential
import site.ftka.survivalcore.essentials.logging.LoggingEssential
import site.ftka.survivalcore.essentials.proprietaryEvents.ProprietaryEventsEssential
import site.ftka.survivalcore.services.ServicesCore
import java.util.concurrent.Executors

class MClass: JavaPlugin() {

    // basics
    val globalScheduler = Executors.newSingleThreadScheduledExecutor()

    // Initialize essentials
    val loggingEssential = LoggingEssential(this)
    val configsEssential = ConfigsEssential(this)
    val dbEssential = DatabaseEssential(this)
    val eventsEssential = ProprietaryEventsEssential(this)

    // Initialize services
    val servicesCore = ServicesCore(this)

    var starting: Boolean = true
    var stopping: Boolean = false

    override fun onEnable() {
        // loggingEssential does not have init nor restart
        configsEssential.init()
        dbEssential.init()
        servicesCore.initAll()
        // eventsEssential does not have init nor restart

        initListeners()

        starting = false

        val invgt = invguitester(this)
    }

    override fun onDisable() {
        stopping = true

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
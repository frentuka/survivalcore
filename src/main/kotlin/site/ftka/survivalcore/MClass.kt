package site.ftka.survivalcore

import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import site.ftka.survivalcore.apps.InventoryGUITester.invguitester
import site.ftka.survivalcore.essentials.configs.ConfigsEssential
import site.ftka.survivalcore.essentials.database.DatabaseEssential
import site.ftka.survivalcore.initless.logging.LoggingInitless
import site.ftka.survivalcore.initless.metrics.MetricsInitless
import site.ftka.survivalcore.initless.proprietaryEvents.ProprietaryEventsInitless
import site.ftka.survivalcore.services.ServicesFramework
import java.util.concurrent.Executors

class MClass: JavaPlugin() {

    // basics
    val globalScheduler = Executors.newSingleThreadScheduledExecutor()

    // Instantiate essentials
    val configsEssential = ConfigsEssential(this)
    val dbEssential = DatabaseEssential(this)

    // Instantiate initless
    val loggingInitless = LoggingInitless(this)
    val metricsInitless = MetricsInitless(this)
    val propEventsInitless = ProprietaryEventsInitless(this)

    // Instantiate services
    val servicesFwk = ServicesFramework(this)

    var starting: Boolean = true
    var stopping: Boolean = false

    override fun onEnable() {
        // initialize all essentials
        configsEssential.init()
        dbEssential.init()
        servicesFwk.initAll()

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
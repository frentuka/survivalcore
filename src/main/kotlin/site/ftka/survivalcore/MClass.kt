package site.ftka.survivalcore

import org.bukkit.command.CommandExecutor
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import site.ftka.survivalcore.apps.AppsFramework
import site.ftka.survivalcore.essentials.EssentialsFramework
import site.ftka.survivalcore.initless.logging.LoggingInitless
import site.ftka.survivalcore.initless.proprietaryEvents.ProprietaryEventsInitless
import site.ftka.survivalcore.services.ServicesFramework
import java.util.concurrent.Executors

class MClass: JavaPlugin(), CommandExecutor {

    // basics
    val globalScheduler = Executors.newSingleThreadScheduledExecutor()

    // Instantiate initless-es
    val loggingInitless = LoggingInitless(this)
    val propEventsInitless = ProprietaryEventsInitless(this)

    // Instantiate essentials
    val essentialsFwk = EssentialsFramework(this)

    // Instantiate services
    val servicesFwk = ServicesFramework(this)

    // Instantiate apps
    val appsFwk = AppsFramework(this)

    var starting: Boolean = true
    var stopping: Boolean = false

    override fun onEnable() {
        // initialize all essentials
        essentialsFwk.initAll()

        // initialize services
        servicesFwk.initAll()

        initListeners()

        // apps
        appsFwk.initAll()

        starting = false

        getCommand("restart")?.setExecutor(this)
    }

    override fun onDisable() {
        stopping = true

        appsFwk.stopAll()
        servicesFwk.stopAll()
        essentialsFwk.stopAll()
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
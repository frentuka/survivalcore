package site.ftka.survivalcore

import org.bukkit.plugin.java.JavaPlugin
import site.ftka.survivalcore.essentials.logging.LoggingEssential
import site.ftka.survivalcore.services.ServicesCore

class MClass: JavaPlugin() {

    // Initialize essentials
    val loggingEssential = LoggingEssential(this)

    // Initialization (safe to initialize listeners)
    val onProxyInitRunnables: MutableList<Runnable> = mutableListOf()
    // Termination (save all pending changes in database AND write backup in storage)
    val onProxyTermRunnables: MutableList<Runnable> = mutableListOf()

    // Initialize services
    lateinit var services: ServicesCore

    override fun onEnable() {
        services = ServicesCore(this)
        onProxyInitRunnables.forEach(Runnable::run)
    }

    override fun onDisable() {
        onProxyTermRunnables.forEach(Runnable::run)
    }



}
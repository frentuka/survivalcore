package site.ftka.survivalcore

import org.bukkit.plugin.java.JavaPlugin
import site.ftka.survivalcore.services.ServicesCore

class MClass: JavaPlugin() {

    // Initialization (safe to initialize listeners)
    val onProxyInitRunnables: MutableList<Runnable> = mutableListOf()
    // Termination (save all pending changes in database AND write backup in storage)
    val onProxyTermRunnables: MutableList<Runnable> = mutableListOf()

    // Initialize services
    private lateinit var services: ServicesCore
    fun services(): ServicesCore {
        if (services == null) services = ServicesCore(this)
        return services
    }

    override fun onEnable() {
        onProxyInitRunnables.forEach(Runnable::run)
    }

    override fun onDisable() {
        onProxyTermRunnables.forEach(Runnable::run)
    }



}
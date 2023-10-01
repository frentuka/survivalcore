package site.ftka.proxycore

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import site.ftka.proxycore.services.ServicesCore
import java.nio.file.Path
import java.util.logging.Logger

@Plugin(id = "proxycore", name = "Proxy Core", authors = ["srleg"])
class MClass @Inject
constructor(val server: ProxyServer, val logger: Logger, @DataDirectory val dataDirectory: Path) {

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

    @Subscribe
    fun onProxyInitialization(e: ProxyInitializeEvent?) {
        services = ServicesCore(this)
        onProxyInitRunnables.forEach(Runnable::run)
    }

    @Subscribe
    fun onProxyTermination(e: ProxyShutdownEvent) {
        onProxyTermRunnables.forEach(Runnable::run)
    }
}
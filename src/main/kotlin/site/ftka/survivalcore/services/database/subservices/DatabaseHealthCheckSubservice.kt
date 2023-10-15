package site.ftka.survivalcore.services.database.subservices

import kotlinx.coroutines.*
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.database.dbService
import site.ftka.survivalcore.services.database.events.DatabaseHealthCheckFailedEvent
import site.ftka.survivalcore.services.database.events.DatabaseReconnectEvent

class DatabaseHealthCheckSubservice(private val service: dbService, private val plugin: MClass, private val interval: Long) {

    private var healthCheckJob: Job? = null

    private val checkFailEvent = DatabaseHealthCheckFailedEvent()
    private val reconnectEvent = DatabaseReconnectEvent()

    fun start() {
        healthCheckJob = CoroutineScope(Dispatchers.Default).launch { healthCheck(); delay(interval) }
    }

    fun stop() {
        healthCheckJob?.cancel()
    }

    private fun healthCheck() {
        service.asyncPing().whenCompleteAsync{ result, _ ->
            if (result && service.health) return@whenCompleteAsync

            if (!result && service.health) {
                service.health = false
                plugin.server.pluginManager.callEvent(checkFailEvent)
            }

            if (result && !service.health) {
                service.health = true
                plugin.server.pluginManager.callEvent(reconnectEvent)
            }
        }
    }

}
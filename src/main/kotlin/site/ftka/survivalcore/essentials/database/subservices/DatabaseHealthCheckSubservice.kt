package site.ftka.survivalcore.essentials.database.subservices

import kotlinx.coroutines.*
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.database.DatabaseEssential
import site.ftka.survivalcore.essentials.database.events.DatabaseHealthCheckFailedEvent
import site.ftka.survivalcore.essentials.database.events.DatabaseReconnectEvent

class DatabaseHealthCheckSubservice(private val service: DatabaseEssential, private val plugin: MClass, private val interval: Long) {

    private var firstRun = true

    private var healthCheckJob: Job? = null

    private val checkFailEvent = DatabaseHealthCheckFailedEvent()
    private val reconnectEvent = DatabaseReconnectEvent()

    fun start() {
        healthCheckJob = CoroutineScope(Dispatchers.Default).launch { delay(interval); healthCheck() }
    }

    fun stop() {
        healthCheckJob?.cancel()
    }

    private fun healthCheck() {
        service.asyncPing().whenCompleteAsync{ result, _ ->
            if (firstRun) {
                service.health = result
                return@whenCompleteAsync
            }

            if (result && service.health) return@whenCompleteAsync

            if (!result && service.health) {
                service.health = false
                plugin.server.pluginManager.callEvent(checkFailEvent)
                return@whenCompleteAsync
            }

            if (result && !service.health) {
                service.health = true
                plugin.server.pluginManager.callEvent(reconnectEvent)
                return@whenCompleteAsync
            }
        }
    }

}
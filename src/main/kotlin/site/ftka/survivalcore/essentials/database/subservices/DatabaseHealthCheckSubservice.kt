package site.ftka.survivalcore.essentials.database.subservices

import io.lettuce.core.RedisChannelHandler
import io.lettuce.core.RedisConnectionStateListener
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.database.DatabaseEssential
import site.ftka.survivalcore.essentials.database.events.DatabaseDisconnectEvent
import site.ftka.survivalcore.essentials.database.events.DatabaseReconnectEvent
import java.net.SocketAddress

class DatabaseHealthCheckSubservice(val essential: DatabaseEssential, val plugin: MClass): RedisConnectionStateListener {

    private val checkFailEvent = DatabaseDisconnectEvent()
    private val reconnectEvent = DatabaseReconnectEvent()

    override fun onRedisConnected(connection: RedisChannelHandler<*, *>?, socketAddress: SocketAddress?) {
        if (essential.health)
            return

        essential.health = true
        plugin.propEventsInitless.fireEvent(reconnectEvent)
    }

    override fun onRedisDisconnected(connection: RedisChannelHandler<*, *>?) {
        if (!essential.health)
            return

        if (plugin.stopping)
            return

        essential.health = false
        plugin.propEventsInitless.fireEvent(checkFailEvent)
    }

}
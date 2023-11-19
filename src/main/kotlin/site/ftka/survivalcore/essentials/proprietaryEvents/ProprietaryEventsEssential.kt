package site.ftka.survivalcore.essentials.proprietaryEvents

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.essentials.logging.LoggingEssential
import site.ftka.survivalcore.essentials.proprietaryEvents.annotations.PropEventHandler
import site.ftka.survivalcore.essentials.proprietaryEvents.enums.PropEventPriority
import site.ftka.survivalcore.essentials.proprietaryEvents.interfaces.PropListener
import site.ftka.survivalcore.essentials.proprietaryEvents.objects.PropEvent
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

class ProprietaryEventsEssential(private val plugin: MClass) {
    private val logger = plugin.loggingEssential.getLog("Events", Component.text("Events"))

    /*
        Recreate Bukkit's event system
        to not depend on it.
     */

    private val listeners = mutableListOf<PropListener>()

    // register a listener
    fun registerListener(listener: PropListener) = listeners.add(listener)

    // unregister a listener
    fun unregisterListener(listener: PropListener) = listeners.remove(listener)

    // prepare call for listeners when event is fired
    fun fireEvent(event: PropEvent) {

        logger.log("Firing event: $event", LoggingEssential.LogLevel.DEBUG)

        val listenersToBeCalled = mutableMapOf<PropListener, kotlin.reflect.KFunction<*>>()

        for (listener in listeners) {

            logger.log("Detected listener: $listener", LoggingEssential.LogLevel.DEBUG)

            // loop every function
            listener::class.memberFunctions.filter {
                it.findAnnotation<PropEventHandler>() != null && // only functions with @PropEventHandler annotation
                it.parameters.size == 2 && // only functions with 1 parameter
                it.parameters[1].type.classifier == event::class // only parameters equal to the event type
            }.forEach{
                listenersToBeCalled[listener] = it
            }

            logger.log("Added members to be called: ${listenersToBeCalled.size}")
        }

        // callListeners will process it's priority
        callListeners(listenersToBeCalled, event)
    }

    // will call listeners taking in count it's priority
    private fun callListeners(listenerMembers: MutableMap<PropListener, kotlin.reflect.KFunction<*>>, event: PropEvent) {

        // Iterate through priorities from first to last.
        val lastPriority = PropEventPriority.MONITOR.ordinal
        for (priorityIndex in 0..lastPriority) {

            logger.log("Calling event $event for priority level $priorityIndex", LoggingEssential.LogLevel.DEBUG)

            // for every listener...
            for (member in listenerMembers) {
                logger.log("Processing member $member", LoggingEssential.LogLevel.DEBUG)

                val eventHandlerTag = member.value.findAnnotation<PropEventHandler>()
                // if the listener priority is right
                if (eventHandlerTag?.priority?.ordinal == priorityIndex) {
                    logger.log("Successfully calling event for member $member", LoggingEssential.LogLevel.DEBUG)
                    member.value.call(member.key, event) // call listener! (arg1: owner, the listener. arg2: event)
                    listenerMembers.remove(member.key) // should not be taken in count for next iterations after being called
                }
            }

        }
    }

}
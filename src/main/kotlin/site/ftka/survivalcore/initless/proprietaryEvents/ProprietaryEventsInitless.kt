package site.ftka.survivalcore.initless.proprietaryEvents

import net.kyori.adventure.text.Component
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.initless.logging.LoggingInitless
import site.ftka.survivalcore.initless.proprietaryEvents.annotations.PropEventHandler
import site.ftka.survivalcore.initless.proprietaryEvents.enums.PropEventPriority
import site.ftka.survivalcore.initless.proprietaryEvents.interfaces.PropListener
import site.ftka.survivalcore.initless.proprietaryEvents.objects.PropEvent
import kotlin.reflect.KClassifier
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

class ProprietaryEventsInitless(private val plugin: MClass) {
    private val logger = plugin.loggingInitless.getLog("Events", Component.text("Events"))

    /*
        Recreate Bukkit's event system
        to not depend on it.
     */

    // Will save listener data for it to not be processed again
    // As it constantly uses reflection, processing it multiple times
    // would end in high cpu usage
    private data class CallableFunction(val owner: PropListener, val function: KFunction<*>, val priority: PropEventPriority)
    private val functions = mutableMapOf<CallableFunction, KClassifier>() // <Function, Event>

    // register a listener
    fun registerListener(listener: PropListener) {
        val properListenerClassName = listener.toString().split(".").last().split("@").first()
        logger.log("Received listener to register: $properListenerClassName", LoggingInitless.LogLevel.DEBUG)

        // loop every function
        listener::class.memberFunctions.filter {
            it.findAnnotation<PropEventHandler>() != null && // only functions with @PropEventHandler annotation
                    it.parameters.size == 2 // only functions with 1 parameter
        }.forEach{
            logger.log("Discovered callable function: ${it.name}", LoggingInitless.LogLevel.DEBUG)

            // save to be called in the future
            val priority = it.findAnnotation<PropEventHandler>()!!.priority
            val newFun = CallableFunction(listener, it, priority)

            val classifier = it.parameters[1].type.classifier as KClassifier
            functions[newFun] = classifier
        }
    }

    // unregister a listener
    fun unregisterListener(listener: PropListener) {
        val iterator = functions.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key.owner == listener)
                iterator.remove()
        }
    }
    
    /*
            PROCESSING
     */

    // gather listeners to be called
    fun fireEvent(event: PropEvent) {
        logger.log("Firing event: ${event.name}", LoggingInitless.LogLevel.DEBUG)

        // <CallableFunction, Priority>
        val eventFunctions = mutableMapOf<CallableFunction, Int>()

        for (function in functions)
            if (function.value == event::class) eventFunctions[function.key] = function.key.priority.ordinal

        val firstVal = PropEventPriority.FIRST.ordinal
        val lastVal = PropEventPriority.MONITOR.ordinal
        for (priorityIndex in firstVal..lastVal) {
            val functionsToBeCalled = eventFunctions.filter { it.value == priorityIndex }.keys

            functionsToBeCalled.forEach{
                val className = it.owner.javaClass.name.split(".").last()
                logger.log("${event.name} -> $className.${it.function.name} (${it.priority})")
                it.function.call(it.owner, event)
            }
        }

    }

}
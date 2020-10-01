package com.kev.framework

import android.os.Handler
import android.os.Looper
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject

/**
 * Class that defines a 'scope' of events to be used for subscription / emission
 */
class EventScope {
    var handlers = HashMap<KClass<*>, ArrayList<Event.EventHandler<*>>>()

    /**
     * Define a handler to run when the event is emit
     */
    inline fun <reified T> on(handler: Event.EventHandler<T>) {
        val tClass = T::class

        // Add this to our handlers
        handlers[tClass]?.add(handler) ?: run {
            val newList = ArrayList<Event.EventHandler<*>>()
            newList.add(handler)
            handlers[tClass] = newList
        }

        // Add this to the static companion
        val onMethod = tClass.companionObject?.members?.firstOrNull {
            it.name == "on"
        }
        onMethod?.call(tClass.companionObject?.objectInstance, handler)
    }

    /**
     * Clear the scope of handlers
     */
    fun clear() {
        for (entry in handlers) {
            // key == class to call clear method on
            val clearMethod = entry.key.companionObject?.members?.firstOrNull {
                it.name == "clear"
            }
            clearMethod?.let {
                // value == handler list to remove
                for (handler in entry.value) {
                    it.call(entry.key.companionObject?.objectInstance, handler)
                }
            }
        }

        // Finally, clear our map
        handlers.clear()
    }
}

/**
 * Defines an event of templated type to be sent across scopes
 */
@Suppress("UnnecessaryAbstractClass")
open class Event<T>(sticky: Boolean) {
    /**
     * Handler for events that is executed on main looper
     */
    object MainHandler : Handler(Looper.getMainLooper())

    /**
     * Type of handler to react when events are emit
     */
    abstract class EventHandler<T> {
        /**
         * Called when an event is emit -- not guaranteed to be called on main
         */
        open fun on(evt: T) {}

        /**
         * Called when an event is emit -- guaranteed to be called on main
         */
        open fun onMain(evt: T) {}

        /**
         * Clear the handler from the scope
         */
        inline fun <reified T> clear() {
            val clearMethod = T::class.companionObject?.members?.firstOrNull {
                it.name == "clear"
            }
            clearMethod?.call(T::class.companionObject?.objectInstance, this)
        }
    }

    private val isSticky = sticky
    var lastEmit: T? = null

    var handlers = listOf<EventHandler<T>>()

    /**
     * Calls all assigned handlers with emit events
     */
    fun on(handler: EventHandler<T>): EventHandler<T> {
        handlers += handler

        if (isSticky) {
            emit(lastEmit ?: return handler)
        }

        return handler
    }

    /**
     * Clears all assigned handlers of type
     */
    fun clear(handler: EventHandler<T>) {
        handlers -= handler
    }

    /**
     * Emits event on all assigned handlers
     */
    fun emit(evt: T) {
        for (handler in handlers) {
            handler.on(evt)

            // Push to the main handler
            MainHandler.post {
                handler.onMain(evt)
            }
        }

        lastEmit = evt
    }
}

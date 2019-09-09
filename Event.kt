package com.kev.framework

import android.os.Handler
import android.os.Looper
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject

class EventScope {
    var handlers = HashMap<KClass<*>, ArrayList<Event.EventHandler<*>>>()

    inline fun<reified T> on(handler: Event.EventHandler<T>) {
        val tClass = T::class

        // Add this to our handlers
        handlers[tClass]?.let {
            it.add(handler)
        } ?: run {
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

    fun clear() {
        for(entry in handlers) {
            // key == class to call clear method on
            val clearMethod = entry.key.companionObject?.members?.firstOrNull {
                it.name == "clear"
            }
            clearMethod?.let {
                // value == handler list to remove
                for(handler in entry.value) {
                    it.call(entry.key.companionObject?.objectInstance, handler)
                }
            }
        }

        // Finally, clear our map
        handlers.clear()
    }
}

open class Event<T>(sticky: Boolean) {
    object MainHandler : Handler(Looper.getMainLooper())

    abstract class EventHandler<T> {
        open fun on(evt: T) {}
        open fun onMain(evt: T) {}
        inline fun<reified T> clear() {
            val clearMethod = T::class.companionObject?.members?.firstOrNull {
                it.name == "clear"
            }
            clearMethod?.call(T::class.companionObject?.objectInstance, this)
        }
    }

    private val isSticky = sticky
    var lastEmit : T? = null

    var handlers = listOf<EventHandler<T>>()
    fun on(handler: EventHandler<T>) : EventHandler<T> {
        handlers += handler

        if(isSticky) {
            emit(lastEmit ?: return handler)
        }

        return handler
    }

    fun clear(handler: EventHandler<T>) {
        handlers -= handler
    }


    fun emit(evt: T) {
        for(handler in handlers) {
            handler.on(evt)

            // Push to the main handler
            MainHandler.post {
                handler.onMain(evt)
            }
        }

        lastEmit = evt
    }
}


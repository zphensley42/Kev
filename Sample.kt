import com.kev.framework.Event
import com.kev.framework.EventScope

/**
 * Sample event with a single input
 */
data class FooEvent(val input: String) {
    companion object : Event<FooEvent>(false)

    /**
     * Emit this event (standard reflection)
     */
    fun emit() = Companion.emit(this)
}

/**
 * Sample class with a scope and method to listen to events
 */
class Bar {
    val eventScope = EventScope()

    /**
     * Method to show how to listen to events
     */
    fun listen() {
        eventScope.on(object : Event.EventHandler<FooEvent>() {
            override fun onMain(evt: FooEvent) {
                // Optional override
                // Do stuff with evt on main
            }

            override fun on(evt: FooEvent) {
                // Optional override
                // Do stuff with evt on emission thread
            }
        })
    }

    /**
     * Method to show how to stop listening / cleanup event listening channels
     */
    fun destroy() {
        eventScope.clear()
    }

    /**
     * Method to emit a test event
     */
    fun testEvent() {
        FooEvent("SomeValue").emit()
    }
}

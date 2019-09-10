import com.kev.framework.*

data class FooEvent(val input: String) {
    companion object : Event<FooEvent>(false)
    fun emit() = Companion.emit(this)
}

class Bar {
    val eventScope = EventScope()

    fun listen() {
        eventScope.on(object: Event.EventHandler<FooEvent>() {
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

    fun destroy() {
        eventScope.clear()
    }
    
    fun testEvent() {
        FooEvent("SomeValue").emit()
    }
}

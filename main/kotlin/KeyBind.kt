package joytypad

open class KeyBind {
    val name: String
    private val events = mutableListOf<PadEvents>()
    private val pressAction: () -> Unit

    constructor(name: String, pressAction: () -> Unit) {
        this.name = name
        this.pressAction = pressAction
    }

    fun bindEvents(vararg events: PadEvents) {
        this.events.addAll(events)
    }

    open fun press() {
        if (isEnable) this.pressAction()
    }

    open fun release() {}

    fun hasEvents(vararg events: PadEvents): Boolean {
        events.forEach {
            if (this.events.contains(it)) return true
        }
        return false
    }

    fun hasAllEvents(vararg events: PadEvents): Boolean {
        if (this.events.size != events.size) return false
        events.forEach {
            if (!this.events.contains(it)) return false
        }
        return true
    }

    fun hasNoEvents(vararg events: PadEvents): Boolean {
        events.forEach {
            if (this.events.contains(it)) return false
        }
        return true
    }
}

class LongPressKeyBind(name: String, pressAction: () -> Unit): KeyBind(name, pressAction) {
    private var isDown = false
    private val waitTime = 300L
    private val repeatSpeed = 100L

    override fun press() {
        if (!isEnable) return
        super.press()
        this.isDown = true
        Thread.sleep(this.waitTime)
        while (this.isDown) {
            if (!isEnable) return
            super.press()
            Thread.sleep(this.repeatSpeed)
        }
    }

    override fun release() {
        if (!isEnable) return
        super.release()
        this.isDown = false
    }
}
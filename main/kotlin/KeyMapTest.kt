import joytypad.KeyMap
import joytypad.PadEvents
import joytypad.getKeyMaps
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class KeyMapTest {

    @Test
    fun hasTags() {
        val keyMaps = getKeyMaps()
        val hoge = keyMaps.filter { it.hasEvents(PadEvents.UP) }
        println(hoge)
    }
}
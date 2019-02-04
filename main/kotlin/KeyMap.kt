package joytypad

import java.awt.event.KeyEvent
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class KeyMap {
    val nan: Onsetsu = Onsetsu()
    val top: Onsetsu = Onsetsu()
    val topRight: Onsetsu = Onsetsu()
    val right: Onsetsu = Onsetsu()
    val downRight: Onsetsu = Onsetsu()
    val down: Onsetsu = Onsetsu()
    val downLeft: Onsetsu = Onsetsu()
    val left: Onsetsu = Onsetsu()
    val topLeft: Onsetsu = Onsetsu()
    val pressed: Onsetsu = Onsetsu()

    fun onsetsu(direction: Direction): Onsetsu {
        return when (direction) {
            Direction.NAN -> this.nan
            Direction.TOP -> this.top
            Direction.TOP_LEFT -> this.topLeft
            Direction.LEFT -> this.left
            Direction.DOWN_LEFT -> this.downLeft
            Direction.DOWN -> this.down
            Direction.DOWN_RIGHT -> this.downRight
            Direction.RIGHT -> this.right
            Direction.TOP_RIGHT -> this.topRight
            Direction.PRESSED -> this.pressed
        }
    }
}

class Onsetsu {
    val seion: Outputs = Outputs()
    val dakuon: Outputs = Outputs()
    val handakuon: Outputs = Outputs()
    val sokuon: Outputs = Outputs()

    fun outputs(inputMode: InputMode): Outputs {
        return when (inputMode) {
            InputMode.SEION -> this.seion
            InputMode.DAKUON -> this.dakuon
            InputMode.HANDAKUON -> this.handakuon
            InputMode.SOKUON -> this.sokuon
        }
    }
}

/**
 * ゲームパッドのボタンの番号を表現します
 */
typealias ButtonNum = String

class Outputs {
    private var one: Output = Output()
    private var two: Output = Output()
    private var three: Output = Output()
    private var four: Output = Output()
    private var five: Output = Output()

    fun one(text: String, vararg keycode: String): Two {
        this.one = Output(text, getKeyCodes(keycode))
        return this.Two()
    }

    inner class Two {
        fun two(text: String, vararg keycode: String): Three {
            two = Output(text, getKeyCodes(keycode))
            return Three()
        }
    }

    inner class Three {
        fun three(text: String, vararg keycode: String): Four {
            three = Output(text, getKeyCodes(keycode))
            return Four()
        }
    }

    inner class Four {
        fun four(text: String, vararg keycode: String): Five {
            four = Output(text, getKeyCodes(keycode))
            return Five()
        }
    }

    inner class Five {
        fun five(text: String, vararg keycode: String) {
            five = Output(text, getKeyCodes(keycode))
        }
    }

    fun output(button: ButtonNum): Output {
        return when (button) {
            TypeButton.ONE.num -> this.one
            TypeButton.TWO.num -> this.two
            TypeButton.THREE.num -> this.three
            TypeButton.FOUR.num -> this.four
            TypeButton.FIVE.num -> this.five
            else -> Output()
        }
    }

    fun getKeyCodes(keys: Array<out String>): List<Int> {
        return keys.map { s -> getKeyEvent(s) }.filterNotNull()
    }

    fun getKeyEvent(key: String): Int? {
        val name = "VK_${key.toUpperCase()}"
        val clazz: Class<KeyEvent> = KeyEvent::class.java
        val field: Field? = clazz.fields
            .filter { field: Field? -> field?.name == name }
            .firstOrNull()
        val keyEvent = field?.get(clazz)
        return when (keyEvent) {
            is Int -> keyEvent
            else -> null
        }
    }
}

data class Output(
    val text: String = "",
    val keycode: List<Int> = listOf()
)

enum class Direction {
    TOP, DOWN, RIGHT, LEFT, TOP_RIGHT, TOP_LEFT, DOWN_RIGHT, DOWN_LEFT, NAN, PRESSED
}

enum class InputMode {
    SEION, DAKUON, HANDAKUON, SOKUON
}

enum class TypeButton(val num: ButtonNum) {
    ONE("3"),
    TWO("1"),
    THREE("0"),
    FOUR("2"),
    FIVE("11")
}

enum class EditButton(val num: ButtonNum) {
    ENTER("6"),
    BACK_SPACE("7"),
    SPACE("9"),
    ZEN_HAN("12")
}

class EditKeyMap {
    val enter = listOf(KeyEvent.VK_ENTER)
    val backSpace = listOf(KeyEvent.VK_BACK_SPACE)
    val space = listOf(KeyEvent.VK_SPACE)
    val zenHan = listOf(244)

    fun get(num: ButtonNum): List<Int> {
        return when (num) {
            EditButton.ENTER.num -> this.enter
            EditButton.BACK_SPACE.num -> this.backSpace
            EditButton.SPACE.num -> this.space
            EditButton.ZEN_HAN.num -> this.zenHan
            else -> listOf()
        }
    }
}
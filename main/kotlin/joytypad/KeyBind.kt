package joytypad

import java.awt.event.KeyEvent
import java.lang.reflect.Field

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

    fun matchPerfectly(vararg events: PadEvents): Boolean {
        if (this.events.size != events.size) return false
        events.forEach {
            if (!this.events.contains(it)) return false
        }
        return true
    }

    fun hasAllEvents(vararg events: PadEvents): Boolean {
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
    private val waitTime = 700L
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

/**
 * 文字列をKeyEventへと変換します
 */
private fun String.toKeyEvent(): Int? {
    val name = "VK_${this.toUpperCase()}"
    val clazz: Class<KeyEvent> = KeyEvent::class.java
    val field: Field? = clazz.fields
        .filter { it?.name == name }
        .firstOrNull()
    val keyEvent = field?.get(clazz)
    return when (keyEvent) {
        is Int -> keyEvent
        else -> null
    }
}

fun String?.toArray(): Array<String> {
    return this?.split("")?.toTypedArray() ?: arrayOf("")
}

/**
 * 五十音すべてを網羅したKeyMapのListを返します。
 * @return 五十音すべてを網羅したKeyMapのList
 */
fun getKeyMaps(): MutableList<KeyBind> {
    val keyEventBind = { name: String, keyEvents: List<Int> ->
        KeyBind(name) {
            keyEvents.forEach {
                robot.keyPress(it)
            }
            keyEvents.forEach {
                robot.keyRelease(it)
            }
        }
    }

    val typeBind = { name: String, keys: List<String> ->
        val keyEvents = keys.mapNotNull { it.toKeyEvent() }
        keyEventBind(name, keyEvents)
    }

    val longPressBind = { name: String, keys: List<String> ->
        val keyEvents = keys.mapNotNull { it.toKeyEvent() }
        LongPressKeyBind(name) {
            keyEvents.forEach { robot.keyPress(it) }
            keyEvents.forEach { robot.keyRelease(it) }
        }
    }

    val kanaTable = getHiraganaTable()
    val keyMaps = mutableListOf<KeyBind>()

    keyMaps.addAll(run {
        val specialCharacters = mutableListOf<KeyBind>()

        specialCharacters.add(
            longPressBind("Back Space", listOf("back_space"))
                .apply { this.bindEvents(PadEvents.BACK) }
        )

        specialCharacters.add(
            typeBind("Space", listOf("space"))
                .apply { this.bindEvents(PadEvents.LT) }
        )

        specialCharacters.add(
            keyEventBind("全角/半角", listOf(244))
                .apply { this.bindEvents(PadEvents.POWER) }
        )


        specialCharacters.add(
            typeBind("Enter", listOf("enter"))
                .apply { this.bindEvents(PadEvents.START) }
        )

        specialCharacters.add(
            longPressBind("↑", listOf("up"))
                .apply { this.bindEvents(PadEvents.POV_UP) }
        )

        specialCharacters.add(
            longPressBind("→", listOf("right"))
                .apply { this.bindEvents(PadEvents.POV_RIGHT) }
        )

        specialCharacters.add(
            longPressBind("↓", listOf("down"))
                .apply { this.bindEvents(PadEvents.POV_DOWN) }
        )

        specialCharacters.add(
            longPressBind("←", listOf("left"))
                .apply { this.bindEvents(PadEvents.POV_LEFT) }
        )

        specialCharacters.add(
            longPressBind("SHIFT + →", listOf("shift" ,"right"))
                .apply { this.bindEvents(PadEvents.LB, PadEvents.POV_RIGHT) }
        )

        specialCharacters.add(
            longPressBind("SHIFT + ←", listOf("shift", "left"))
                .apply { this.bindEvents(PadEvents.LB, PadEvents.POV_LEFT) }
        )

        specialCharacters.forEach { it.bindEvents(PadEvents.CENTER) }

        specialCharacters.add(
            typeBind("ー", listOf("minus"))
                .apply { this.bindEvents(PadEvents.PRESS, PadEvents.RT) }
        )

        specialCharacters.add(
            typeBind("!", listOf("SHIFT", "1"))
                .apply { this.bindEvents(PadEvents.RB, PadEvents.UP_RIGHT, PadEvents.Y) }
        )

        specialCharacters.add(
            typeBind("?", listOf("SHIFT", "SLASH"))
                .apply { this.bindEvents(PadEvents.RB, PadEvents.UP_RIGHT, PadEvents.A) }
        )

        specialCharacters.add(
            typeBind("(", listOf("SHIFT", "8"))
                .apply { this.bindEvents(PadEvents.RB, PadEvents.UP_RIGHT, PadEvents.X) }
        )

        specialCharacters.add(
            typeBind(")", listOf("SHIFT", "9"))
                .apply { this.bindEvents(PadEvents.RB, PadEvents.UP_RIGHT, PadEvents.B) }
        )

        specialCharacters.add(
            typeBind("、", listOf("COMMA"))
                .apply { this.bindEvents(PadEvents.LEFT, PadEvents.B) }
        )

        specialCharacters.add(
            typeBind("。", listOf("PERIOD"))
                .apply { this.bindEvents(PadEvents.LEFT, PadEvents.X) }
        )

        specialCharacters.add(
            typeBind("F7", listOf("F7"))
                .apply { this.bindEvents(PadEvents.LB, PadEvents.LT) }
        )

        specialCharacters
    })

    val getGyou = {a: String, i: String, u: String, e: String, o: String, direction: PadEvents ->
        val gyou = mutableListOf<KeyBind>()
        gyou.add(typeBind(a, kanaTable[a]?.split("") ?: listOf()))
        gyou.add(typeBind(i, kanaTable[i]?.split("") ?: listOf()))
        gyou.add(typeBind(u, kanaTable[u]?.split("") ?: listOf()))
        gyou.add(typeBind(e, kanaTable[e]?.split("") ?: listOf()))
        gyou.add(typeBind(o, kanaTable[o]?.split("") ?: listOf()))
        gyou[0].bindEvents(PadEvents.Y)
        gyou[1].bindEvents(PadEvents.B)
        gyou[2].bindEvents(PadEvents.A)
        gyou[3].bindEvents(PadEvents.X)
        gyou[4].bindEvents(PadEvents.RT)
        gyou.forEach { it.bindEvents(direction) }
        gyou
    }

    keyMaps.addAll( run {
        val seion = mutableListOf<KeyBind>()
        seion.addAll(getGyou("あ", "い", "う", "え", "お", PadEvents.CENTER))
        seion.addAll(getGyou("か", "き", "く", "け", "こ", PadEvents.UP))
        seion.addAll(getGyou("さ", "し", "す", "せ", "そ", PadEvents.UP_RIGHT))
        seion.addAll(getGyou("た", "ち", "つ", "て", "と", PadEvents.RIGHT))
        seion.addAll(getGyou("な", "に", "ぬ", "ね", "の", PadEvents.DOWN_RIGHT))
        seion.addAll(getGyou("は", "ひ", "ふ", "へ", "ほ", PadEvents.DOWN))
        seion.addAll(getGyou("ま", "み", "む", "め", "も", PadEvents.DOWN_LEFT))
        seion.addAll(getGyou("や", "", "ゆ", "", "よ", PadEvents.LEFT))
        seion.addAll(getGyou("ら", "り", "る", "れ", "ろ", PadEvents.UP_LEFT))
        seion.addAll(getGyou("わ", "を", "ん", "", "", PadEvents.PRESS))
        seion
    })

    keyMaps.addAll(run {
        val dakuon = mutableListOf<KeyBind>()
        dakuon.addAll(getGyou("が", "ぎ", "ぐ", "げ", "ご", PadEvents.UP))
        dakuon.addAll(getGyou("ざ", "じ", "ず", "ぜ", "ぞ", PadEvents.UP_RIGHT))
        dakuon.addAll(getGyou("だ", "ぢ", "づ", "で", "ど", PadEvents.RIGHT))
        dakuon.addAll(getGyou("ば", "び", "ぶ", "べ", "ぼ", PadEvents.DOWN))
        dakuon.forEach { it.bindEvents(PadEvents.LB) }
        dakuon
    })

    keyMaps.addAll(run {
        val handakuon = mutableListOf<KeyBind>()
        handakuon.addAll(getGyou("0", "1", "2", "3", "4", PadEvents.CENTER))
        handakuon.addAll(getGyou("5", "6", "7", "8", "9", PadEvents.UP))
        handakuon.addAll(getGyou("ぱ", "ぴ", "ぷ", "ぺ", "ぽ", PadEvents.DOWN))
        handakuon.forEach { it.bindEvents(PadEvents.RB) }
        handakuon
    })

    keyMaps.addAll(run {
        val sokuon = mutableListOf<KeyBind>()
        sokuon.addAll(getGyou("ぁ", "ぃ", "ぅ", "ぇ", "ぉ", PadEvents.CENTER))
        sokuon.addAll(getGyou("", "", "っ", "", "", PadEvents.RIGHT))
        sokuon.addAll(getGyou("ゃ", "", "ゅ", "", "ょ", PadEvents.LEFT))
        sokuon.forEach { it.bindEvents(PadEvents.LB, PadEvents.RB) }
        sokuon
    })

    return keyMaps
}

/**
 * 五十音とローマ字の対応表のMapを返します。
 * @return ひらがなをkey、ローマ字をvalueに割り当てたMap
 */
fun getHiraganaTable(): MutableMap<String, String> {
    val m = mutableMapOf<String, String>()
    m["0"] = "0"
    m["1"] = "1"
    m["2"] = "2"
    m["3"] = "3"
    m["4"] = "4"
    m["5"] = "5"
    m["6"] = "6"
    m["7"] = "7"
    m["8"] = "8"
    m["9"] = "9"
    m["あ"] = "a"
    m["い"] = "i"
    m["う"] = "u"
    m["え"] = "e"
    m["お"] = "o"
    m["か"] = "ka"
    m["き"] = "ki"
    m["く"] = "ku"
    m["け"] = "ke"
    m["こ"] = "ko"
    m["さ"] = "sa"
    m["し"] = "shi"
    m["す"] = "su"
    m["せ"] = "se"
    m["そ"] = "so"
    m["た"] = "ta"
    m["ち"] = "chi"
    m["つ"] = "tu"
    m["て"] = "te"
    m["と"] = "to"
    m["な"] = "na"
    m["に"] = "ni"
    m["ぬ"] = "nu"
    m["ね"] = "ne"
    m["の"] = "no"
    m["は"] = "ha"
    m["ひ"] = "hi"
    m["ふ"] = "fu"
    m["へ"] = "he"
    m["ほ"] = "ho"
    m["ま"] = "ma"
    m["み"] = "mi"
    m["む"] = "mu"
    m["め"] = "me"
    m["も"] = "mo"
    m["や"] = "ya"
    m["ゆ"] = "yu"
    m["よ"] = "yo"
    m["ら"] = "ra"
    m["り"] = "ri"
    m["る"] = "ru"
    m["れ"] = "re"
    m["ろ"] = "ro"
    m["わ"] = "wa"
    m["を"] = "wo"
    m["ん"] = "nn"
    m["が"] = "ga"
    m["ぎ"] = "gi"
    m["ぐ"] = "gu"
    m["げ"] = "ge"
    m["ご"] = "go"
    m["ざ"] = "za"
    m["じ"] = "zi"
    m["ず"] = "zu"
    m["ぜ"] = "ze"
    m["ぞ"] = "zo"
    m["だ"] = "da"
    m["ぢ"] = "di"
    m["づ"] = "du"
    m["で"] = "de"
    m["ど"] = "do"
    m["ば"] = "ba"
    m["び"] = "bi"
    m["ぶ"] = "bu"
    m["べ"] = "be"
    m["ぼ"] = "bo"
    m["ぱ"] = "pa"
    m["ぴ"] = "pi"
    m["ぷ"] = "pu"
    m["ぺ"] = "pe"
    m["ぽ"] = "po"
    m["ぁ"] = "la"
    m["ぃ"] = "li"
    m["ぅ"] = "lu"
    m["ぇ"] = "le"
    m["ぉ"] = "lo"
    m["っ"] = "ltu"
    m["ゃ"] = "lya"
    m["ゅ"] = "lyu"
    m["ょ"] = "lyo"
    return m
}
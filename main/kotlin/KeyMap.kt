package joytypad

import java.awt.event.KeyEvent
import java.lang.reflect.Field

class KeyMap {
    val tags = mutableListOf<String>()

    var text: String
        private set

    var keyEvents = mutableListOf<Int>()
        private set

    /**
     * @param text GUI上に表示される文字
     * @param keys エミュレートするキーボードのキー。
     *              "か"の設定をしたい場合はKeyMap("か", "k", "a")としてください。
     *              "@"などの特殊文字を設定したい場合はKeyMap("@", "at")のように
     *              対応する文字列を与えてください。
     */
    constructor(text: String, vararg keys: String) {
        this.text = text
        keys.forEach {
            val keyEvent = it.toKeyEvent()
            if (keyEvent != null) this.keyEvents.add(keyEvent)
        }
    }

    constructor(text: String, vararg keys: Int) {
        this.text = text
        keys.forEach {
            this.keyEvents.add(it)
        }
    }

    fun addTags(vararg tags: String) {
        this.tags.addAll(tags)
    }

    /**
     * タグを持っているかを判定します。持っている場合、trueを返します。
     * @param tags 検索したいタグ
     * @param isExactMatch 完全一致か部分一致かを設定する引数。完全一致判定をしたい場合、trueを与えてください。
     */
    fun hasTags(vararg tags: String, isExactMatch: Boolean = false): Boolean {
        if (isExactMatch && this.tags.size != tags.size) return false
        return this.tags.containsAll(listOf(*tags))
    }

    /**
     * 指定したタグを持っていないかを判定します。ひとつでも指定したタグ持っている場合、falseを返します。
     * @param tags 検索したいタグ
     */
    fun hasNoTags(vararg tags: String): Boolean {
        tags.forEach { tag: String ->
            if (this.tags.contains(tag)) return false
        }
        return true
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

/**
 * ゲームパッドのボタンの番号を表現します
 */
typealias ButtonNum = String

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
        }
    }

    val typeBind = { name: String, keys: List<String> ->
        val keyEvents = keys.mapNotNull { it.toKeyEvent() }
        keyEventBind(name, keyEvents)
    }

    val longPressBind = { name: String, key: String ->
        val keyEvent = key.toKeyEvent()
        LongPressKeyBind(name) {
            if (keyEvent != null) robot.keyPress(keyEvent)
        }
    }

    val kanaTable = getHiraganaTable()
    val keyMaps = mutableListOf<KeyBind>()

    keyMaps.addAll(run {
        val specialCharacters = mutableListOf<KeyBind>()

        specialCharacters.add(
            longPressBind("Back Space", "back_space")
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
            longPressBind("↑", "up")
                .apply { this.bindEvents(PadEvents.POV_UP) }
        )

        specialCharacters.add(
            longPressBind("→", "right")
                .apply { this.bindEvents(PadEvents.POV_RIGHT) }
        )

        specialCharacters.add(
            longPressBind("↓", "down")
                .apply { this.bindEvents(PadEvents.POV_DOWN) }
        )

        specialCharacters.add(
            longPressBind("←", "left")
                .apply { this.bindEvents(PadEvents.POV_LEFT) }
        )

        specialCharacters.forEach { it.bindEvents(PadEvents.CENTER) }

        specialCharacters.add(
            typeBind("ー", listOf("minus"))
                .apply { this.bindEvents(PadEvents.PRESS, PadEvents.RT) }
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
    m["ん"] = "n"
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
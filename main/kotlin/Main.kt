package joytypad

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import net.java.games.input.Component
import net.java.games.input.Component.Identifier
import net.java.games.input.Controller
import net.java.games.input.ControllerEnvironment
import net.java.games.input.Event
import net.java.games.input.EventQueue
import java.awt.Robot
import java.awt.event.KeyEvent

val deadzone: Double = 0.2
var x: Double = 0.0
var y: Double = 0.0
var direction: Direction = Direction.NAN
var isLbDown: Boolean = false
var isRbDown: Boolean = false
var inputMode = InputMode.SEION

val keyMap: KeyMap = KeyMap()
val editKeyMap: EditKeyMap = EditKeyMap()
val robot: Robot = Robot()

fun setKeyMap() {
    keyMap.nan.seion
        .one("あ", "a")
        .two("い", "i")
        .three("う", "u")
        .four("え", "e")
        .five("お", "o")
    keyMap.nan.sokuon
        .one("ぁ", "l", "a")
        .two("ぃ", "l", "i")
        .three("ぅ", "l", "u")
        .four("ぇ", "l", "e")
        .five("ぉ", "l", "o")
    
    keyMap.top.seion
        .one("か", "k", "a")
        .two("き", "k", "i")
        .three("く", "k", "u")
        .four("け", "k", "e")
        .five("こ", "k", "o")
}

/**
 * スティックの方向を返します
 * @param x スティックのx軸の値
 * @param y スティックのy軸の値
 */
fun getDirection(x: Double, y: Double): Direction {
    val degree = Math.atan2(x, y) * 180 / Math.PI
    return when (degree) {
        in -22.5..22.5 -> {
            if (x == 0.0 && y == 0.0) Direction.NAN
            else Direction.DOWN
        }
        in 22.5..67.5 -> Direction.DOWN_RIGHT
        in 67.5..112.5 -> Direction.RIGHT
        in 112.5..157.5 -> Direction.TOP_RIGHT
        in -67.5..-22.5 -> Direction.DOWN_LEFT
        in -112.5..-67.5 -> Direction.LEFT
        in -157.5..-112.5 -> Direction.TOP_LEFT
        else -> Direction.TOP
    }
}

fun main(args: Array<String>) {
    setKeyMap()
    
    val gamepad: Controller = run {
        val controllers = ControllerEnvironment.getDefaultEnvironment().controllers
        for (ctrl in controllers) {
            if (ctrl.type == Controller.Type.STICK) return@run ctrl
        }
        return
    }

    while (true) {
        // コントローラーの初期化?
        // これがないと正常にコントローラーの入力を取得できません
        gamepad.poll()

        // 全てのボタンの状態をスキャンし、左スティック、LB、RBが押し下げられているかを調べます
        // 加えて左スティックのx軸、y軸の値を取得します
        gamepad.components.forEach {
            val identifier = it.identifier
            val name = identifier.name
            val pollData: Double = when (it.pollData) {
                in -deadzone..deadzone -> 0.0
                else -> it.pollData.toDouble()
            }

            when (name) {
                "4" -> {
                    isLbDown = when (pollData) {
                        1.0 -> true
                        else -> false
                    }
                }

                "5" -> {
                    isRbDown = when (pollData) {
                        1.0 -> true
                        else -> false
                    }
                }

                "8" -> direction = when (pollData) {
                    1.0 -> Direction.PRESSED
                    else -> Direction.NAN
                }

                "x" -> x = pollData
                "y" -> y = pollData
            }
        }

        // 左スティックの方向を調べます
        if (direction != Direction.PRESSED) direction = getDirection(x, y)

        // 入力モードを調べます
        inputMode = when {
            isLbDown && isRbDown -> InputMode.SOKUON
            isLbDown && !isRbDown -> InputMode.DAKUON
            !isLbDown && isRbDown -> InputMode.HANDAKUON
            !isLbDown && !isRbDown -> InputMode.SEION
            else -> InputMode.SEION
        }

        // ボタンが押されたときの処理
        val que = gamepad.eventQueue
        val event = Event()
        que@ while (que.getNextEvent(event)) {
            val component = event.component
            val identifier = component.identifier
            val name = identifier.name
            val pollData = component.pollData

            when {
                // LB、RBのイベントは無視します
                name == "4" || name == "5" -> continue@que
                // ボタン以外の入力は無視します
                !name.matches("[0-9]+".toRegex()) -> continue@que
                // ボタンの押し下げは無視します
                pollData != 0.0f -> continue@que
            }

            println("button$name was pressed")
            println("direction: $direction, onsetsu: $inputMode")
            val hoge: List<Int> = run {
                val keycode = keyMap.onsetsu(direction).outputs(inputMode).output(name).keycode
                if (!keycode.isEmpty()) return@run keycode
                else return@run editKeyMap.get(name)
            }

            hoge.forEach { i ->
                robot.keyPress(i)
                robot.keyRelease(i)
            }
        }

        Thread.sleep(25)
    }
}

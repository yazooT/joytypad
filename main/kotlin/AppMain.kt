package joytypad

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import net.java.games.input.Controller
import net.java.games.input.ControllerEnvironment
import net.java.games.input.Event
import java.awt.GraphicsEnvironment
import java.awt.Robot
import javax.swing.JFrame
import javax.xml.bind.JAXBElement
import kotlin.concurrent.thread

private lateinit var stage: Stage

private val keyMaps: MutableList<KeyBind> = getKeyMaps()
private lateinit var seion: ExtParent
private lateinit var dakuon: ExtParent
private lateinit var handakuon: ExtParent
private lateinit var sokuon: ExtParent

val robot = Robot()
var isEnable = true

fun main(args: Array<String>) {
    Platform.setImplicitExit(false)
    Application.launch(AppMain().javaClass)
}

class AppMain: Application(){
    override fun start(stg: Stage) {
        seion = ExtParent(keyMaps
            .filter { it.hasNoEvents(PadEvents.LB, PadEvents.RB) })

        dakuon = ExtParent(keyMaps
            .filter { it.hasEvents(PadEvents.LB) }
            .filter { it.hasNoEvents(PadEvents.RB) })

        handakuon = ExtParent(keyMaps
            .filter { it.hasEvents(PadEvents.RB) }
            .filter { it.hasNoEvents(PadEvents.LB) })

        sokuon = ExtParent(keyMaps
            .filter { it.hasEvents(PadEvents.LB, PadEvents.RB) })

        stage = stg
        stage.title = "JoyTyPad"
        stage.initStyle(StageStyle.TRANSPARENT)

        stage.scene = Scene(seion.parent, Color.TRANSPARENT)

        stage.isAlwaysOnTop = true

        stage.requestFocus()
        stage.show()

        val watcher = GamepadWatcher()
        watcher.start()
    }
}

class GamepadWatcher: Thread() {
    private val gamepad: Controller?
    private val displayWidth: Int
    private val displayHeight: Int
    private val guiWidth = 400
    private val guiHeight = 400

    init {
        val controllers = ControllerEnvironment.getDefaultEnvironment().controllers
        this.gamepad = controllers.filter { it.type == Controller.Type.STICK }.firstOrNull()

        val env = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val displayMode = env.defaultScreenDevice.displayMode
        this.displayWidth = displayMode.width
        this.displayHeight = displayMode.height
    }

    override fun run() {
        while (true) {
            this.monitorGamePad()
            Thread.sleep(30)
        }
    }

    private fun monitorGamePad() {
        // コントローラーの初期化?
        // これがないと正常にコントローラーの入力を取得できません
        if (this.gamepad == null) return
        this.gamepad.poll()

        // 全てのボタンの状態をスキャンし、押し下げられているボタンを取得します。
        // 加えて左スティックのx軸、y軸の値を取得したのち、どの方向を向いているか計算します。
        val gamePadState = mutableListOf<PadEvents>()
        var x = 0.0
        var y = 0.0
        this.gamepad.components.forEach {
            val identifier = it.identifier
            val name = identifier.name
            val pollData: Double = when (it.pollData) {
                in -deadzone..deadzone -> 0.0
                else -> it.pollData.toDouble()
            }

            when (name) {
                "pov" -> {
                    when (pollData) {
                        0.25 -> gamePadState.add(PadEvents.POV_UP)
                        0.50 -> gamePadState.add(PadEvents.POV_RIGHT)
                        0.75 -> gamePadState.add(PadEvents.POV_DOWN)
                        1.00 -> gamePadState.add(PadEvents.POV_LEFT)
                    }
                }
                "x" -> x = pollData
                "y" -> y = pollData
                else -> if (pollData == 1.0) {
                    name.toPadEvent()?.apply { gamePadState.add(this) }
                }
            }
        }

        var currentDirection: PadEvents
        if (gamePadState.contains(PadEvents.PRESS)) {
            currentDirection = PadEvents.PRESS
        } else {
            currentDirection = getDirection(x, y)
            gamePadState.add(currentDirection)
        }

        // GUIを更新します
        this.updateGUI(
            gamePadState.contains(PadEvents.LB),
            gamePadState.contains(PadEvents.RB),
            currentDirection)

        // ボタンが押されたときの処理
        val que = this.gamepad.eventQueue
        val event = Event()
        que@ while (que.getNextEvent(event)) {
            // スティック入力を無視します
            val component = event.component
            if (component.isAnalog) continue@que
            val identifier = component.identifier
            println("${identifier.name}: ${component.pollData}")

            // JoyTyPadの有効/無効を切り替えます
            if (
                identifier.name.toPadEvent() == PadEvents.R_PRESS &&
                component.pollData == 0.0f
            ) {
                when {
                    isEnable -> {
                        isEnable = false
                        Platform.runLater { stage.hide() }
                    }
                    !isEnable -> {
                        isEnable = true
                        Platform.runLater { stage.show() }
                    }
                }
            }

            // 離されたボタンを判別するのは煩雑になるため全てのキーバインドのリリース処理を行います
            if (component.pollData == 0.0f) {
                keyMaps.forEach { it.release() }
                continue@que
            }

            // 入力情報からキーバインド候補を取得します
            val output: KeyBind? = keyMaps
                .firstOrNull { it.hasAllEvents(*gamePadState.toTypedArray()) }

            output ?: continue@que
            thread { output.press() }
        }

    }

    /**
     * シーンを変更します
     * @param isLBDowned LBが押されているかどうか
     * @param isRBDowned RBが押されているかどうか
     */
    private fun updateGUI(isLBDowned: Boolean, isRBDowned: Boolean, direction: PadEvents) {
        val newParent: ExtParent = when {
            !isLBDowned && !isRBDowned -> seion
            isLBDowned && !isRBDowned -> dakuon
            !isLBDowned && isRBDowned -> handakuon
            else -> sokuon
        }
        newParent.changeColor(direction)
        if (stage.scene.root != null) stage.scene.root = newParent.parent
    }

    /**
     * スティックの入力方向を計算します
     */
    private fun getDirection(x: Double, y: Double): PadEvents {
        val degree = Math.atan2(x, y) * 180 / Math.PI
        return when (degree) {
            in -22.5..22.5 -> {
                if (x == 0.0 && y == 0.0) PadEvents.CENTER
                else PadEvents.DOWN
            }
            in 22.5..67.5 -> PadEvents.DOWN_RIGHT
            in 67.5..112.5 -> PadEvents.RIGHT
            in 112.5..157.5 -> PadEvents.UP_RIGHT
            in -67.5..-22.5 -> PadEvents.DOWN_LEFT
            in -112.5..-67.5 -> PadEvents.LEFT
            in -157.5..-112.5 -> PadEvents.UP_LEFT
            else -> PadEvents.UP
        }
    }

    private fun String.toPadEvent(): PadEvents? {
        return PadEvents.values().firstOrNull { it.text == this }
    }
}

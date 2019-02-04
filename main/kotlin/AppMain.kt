package joytypad

import javafx.application.Application
import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.ButtonType
import javafx.scene.layout.AnchorPane
import java.net.URL
import java.util.*

val WIDTH = 600.0
val GAP = 3.0

fun main(args: Array<String>) {
    Application.launch(AppMain().javaClass)
}

class AppMain: Application() {
    lateinit var stage: Stage
    lateinit var controller: Controller

    override fun start(stage: Stage) {
        this.stage = stage
        this.stage.title = "JoyTyPad"

        val fxml = javaClass.getResource("/fxml/joytypad.fxml")
        val fxmlLoader = FXMLLoader(fxml)

        val fxml2 = javaClass.getResource("/fxml/joytypad.fxml")
        val fxmlLoader2 = FXMLLoader(fxml2)

        val parent: Parent = fxmlLoader.load()
        val parent2: Parent = fxmlLoader2.load()

        val grid: GridPane = GridPane()
        grid.hgap = GAP
        grid.hgap = GAP
        grid.add(parent, 0, 0)
        grid.add(parent2, 1, 0)

        this.stage.scene = Scene(grid)
        this.stage.show()

        val keyMap = getKeyMap()

        val ctrl: Controller = fxmlLoader.getController()
        ctrl.setText(keyMap.nan.outputs(InputMode.SEION))

        val ctrl2: Controller = fxmlLoader2.getController()
        ctrl2.setText(keyMap.top.outputs(InputMode.SEION))
//        this.updateScene()
    }
}

fun getKeyMap(): KeyMap {
    val keyMap = KeyMap()

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

    return keyMap
}

class Controller: Initializable {
    @FXML private lateinit var one: Label
    @FXML private lateinit var two: Label
    @FXML private lateinit var three: Label
    @FXML private lateinit var four: Label
    @FXML private lateinit var five: Label

    override fun initialize(location: URL?, resources: ResourceBundle?) {
    }

    fun setText(outputs: Outputs) {
        this.one.text = outputs.output(TypeButton.ONE.num).text
        this.two.text = outputs.output(TypeButton.TWO.num).text
        this.three.text = outputs.output(TypeButton.THREE.num).text
        this.four.text = outputs.output(TypeButton.FOUR.num).text
        this.five.text = outputs.output(TypeButton.FIVE.num).text
    }
}



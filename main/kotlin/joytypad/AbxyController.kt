package joytypad

import com.sun.javafx.css.Style
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.stage.Stage
import java.net.URL
import java.util.*

class AbxyController: Initializable {
    @FXML
    private lateinit var yButton: Label
    @FXML
    private lateinit var bButton: Label
    @FXML
    private lateinit var aButton: Label
    @FXML
    private lateinit var xButton: Label
    @FXML
    private lateinit var rightTrigger: Label

    override fun initialize(location: URL?, resources: ResourceBundle?) {
    }

    fun setText(gyou: List<KeyBind>) {
        Platform.runLater {
            this.yButton.text = gyou.find { it.hasEvents(PadEvents.Y) }?.name ?: ""
            this.bButton.text = gyou.find { it.hasEvents(PadEvents.B) }?.name ?: ""
            this.aButton.text = gyou.find { it.hasEvents(PadEvents.A) }?.name ?: ""
            this.xButton.text = gyou.find { it.hasEvents(PadEvents.X) }?.name ?: ""
            this.rightTrigger.text = gyou.find { it.hasEvents(PadEvents.RT) }?.name ?: ""
        }
    }

    fun changeColor() {
        this.yButton.textFill = Color.YELLOW
        this.bButton.textFill = Color.RED
        this.aButton.textFill = Color.GREEN
        this.xButton.textFill = Color.BLUE
    }

    fun resetColor() {
        this.yButton.textFill = Color.WHITE
        this.bButton.textFill = Color.WHITE
        this.aButton.textFill = Color.WHITE
        this.xButton.textFill = Color.WHITE
    }
}
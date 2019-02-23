package joytypad

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import java.net.URL
import java.util.*

class CheatsheetCtrl: Initializable {
    @FXML
    private lateinit var center: Pane
    @FXML
    private lateinit var up: Pane
    @FXML
    private lateinit var upRight: Pane
    @FXML
    private lateinit var right: Pane
    @FXML
    private lateinit var downRight: Pane
    @FXML
    private lateinit var down: Pane
    @FXML
    private lateinit var downLeft: Pane
    @FXML
    private lateinit var left: Pane
    @FXML
    private lateinit var upLeft: Pane

    override fun initialize(location: URL?, resources: ResourceBundle?) {
    }

    fun setPane(parents: List<Parent>) {
        this.upLeft.children.add(parents[0])
        this.up.children.add(parents[1])
        this.upRight.children.add(parents[2])
        this.left.children.add(parents[3])
        this.center.children.add(parents[4])
        this.right.children.add(parents[5])
        this.downLeft.children.add(parents[6])
        this.down.children.add(parents[7])
        this.downRight.children.add(parents[8])
    }

    fun setPane(parents: Map<PadEvents, Parent>) {
        this.upLeft.children.add(parents[PadEvents.UP_LEFT])
        this.up.children.add(parents[PadEvents.UP])
        this.upRight.children.add(parents[PadEvents.UP_RIGHT])
        this.left.children.add(parents[PadEvents.LEFT])
        this.center.children.add(parents[PadEvents.CENTER])
        this.right.children.add(parents[PadEvents.RIGHT])
        this.downLeft.children.add(parents[PadEvents.DOWN_LEFT])
        this.down.children.add(parents[PadEvents.DOWN])
        this.downRight.children.add(parents[PadEvents.DOWN_RIGHT])
    }

}
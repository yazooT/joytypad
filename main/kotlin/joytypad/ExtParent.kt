package joytypad

import javafx.embed.swing.JFXPanel
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.layout.Pane

class ExtParent {
    var parent: Parent
        private set

    private val controllers = mutableMapOf<PadEvents, AbxyController>()
    private val keyBinds: List<KeyBind>
    private val ABXY_FXML = "/fxml/abxy-circle.fxml"
    private val CHEATSHEET_FXML = "/fxml/cheatsheet.fxml"

    /**
     * @param keyMaps 必ずどのようにLB、RBのフィルターをかけてください。
     *                 例えば濁音のSceneを作る場合、keyBinds.filter { k -> k.hasTags(LB) && k.hasNoTags(RB) }
     */
    constructor(keyMaps: List<KeyBind>) {
        this.keyBinds = keyMaps

        // スティック入力と関連付けたParentとControllerのMapを作ります
        val parents = mutableMapOf<PadEvents, Parent>()
        listOf(
            PadEvents.UP_LEFT, PadEvents.UP, PadEvents.UP_RIGHT,
            PadEvents.LEFT, PadEvents.CENTER, PadEvents.RIGHT,
            PadEvents.DOWN_LEFT, PadEvents.DOWN, PadEvents.DOWN_RIGHT
        ).forEach {
            val pareCtrl: Pair<Parent, AbxyController> = this.getPareCtrl(this.ABXY_FXML)
            parents[it] = pareCtrl.first.apply { this.style = "-fx-background-color: transparent;" }
            this.controllers[it] = pareCtrl.second
        }

        // コントローラーを介し、五十音を割り当てます
        this.controllers.forEach { padEvent, controller ->
            controller.setText(this.keyBinds.filter {
                it.hasEvents(padEvent)
            })
        }

        // abxy-circleをcheatsheetに割り当てます
        val cheatsheet: Pair<Parent, CheatsheetCtrl> = this.getPareCtrl(this.CHEATSHEET_FXML)
        cheatsheet.second.setPane(parents)
        this.parent = cheatsheet.first.apply { this.style = "-fx-background-color: transparent;" }
    }

    private fun <T: Initializable> getPareCtrl(fxmlDir: String): Pair<Parent, T> {
        val fxml = javaClass.getResource(fxmlDir)
        val fxmlLoader = FXMLLoader(fxml)
        val parent: Pane = fxmlLoader.load()
        val ctrl = fxmlLoader.getController<T>()
        return Pair(parent, ctrl)
    }

    fun changeColor(direction: PadEvents) {
        this.controllers.forEach {
            it.value.resetColor()
        }
        this.controllers[direction]?.changeColor()

        this.controllers[PadEvents.CENTER]?.apply {
            if (direction == PadEvents.PRESS) {
                this.setText(keyBinds.filter { it.hasEvents(PadEvents.PRESS) })
                this.changeColor()
            } else {
                this.setText(keyBinds.filter { it.hasEvents(PadEvents.CENTER) })
            }
        }
    }
}
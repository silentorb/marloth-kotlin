package lab

import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Stage
import javafx.animation.Timeline
import javafx.animation.KeyFrame
import javafx.util.Duration
import lab.views.getModelCode
import lab.views.setModelCode
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyEvent


class LabGui : Application() {
  var code: String = ""
  val onCodeChanged: (code: String) -> Unit = setModelCode
  val newCode: () -> String = getModelCode

  override fun start(primaryStage: Stage) {
    primaryStage.title = "Editor"
    val textArea = TextArea()
    textArea.setPrefRowCount(10)
    textArea.setFont(Font.font("Courier", 14.0))

    val updateButton = Button()
    updateButton.text = "Update"
    updateButton.onAction = EventHandler {
        code = textArea.text
        onCodeChanged(code)
    }

    val updater = Timeline(KeyFrame(Duration.seconds(1.0), EventHandler {
      val possibleCode = newCode()
      if (code != possibleCode) {
        code = possibleCode
        textArea.text = code
      }
    }))
    updater.cycleCount = Timeline.INDEFINITE
    updater.play()

    val root = VBox()
    root.children.add(textArea)
    root.children.add(updateButton)
    primaryStage.scene = Scene(root, 300.0, 250.0)

    primaryStage.scene.addEventFilter(KeyEvent.KEY_PRESSED, object : EventHandler<KeyEvent> {
      internal val keyComb: KeyCombination = KeyCodeCombination(KeyCode.S,
          KeyCombination.CONTROL_DOWN)

      override fun handle(event: KeyEvent) {
        if (keyComb.match(event)) {
          code = textArea.text
          onCodeChanged(code)
          event.consume() // <-- stops passing the event to next node
        }
      }
    })

    primaryStage.show()
  }

  companion object {
    @JvmStatic fun main(args: List<String>) {
      Application.launch(LabGui::class.java)
//      launch()
    }
  }
}
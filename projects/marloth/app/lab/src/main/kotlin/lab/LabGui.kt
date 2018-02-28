package lab

import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Stage

class LabGui : Application() {

  override fun start(primaryStage: Stage) {
    primaryStage.title = "Editor"
    val textArea = TextArea()
    textArea.setPrefRowCount(10)
    textArea.setFont(Font.font ("Courier", 14.0))

    val btn = Button()
    btn.text = "Update"
    btn.onAction = EventHandler { println("Hello World!") }

    val root = VBox()
    root.children.add(textArea)
    root.children.add(btn)
    primaryStage.scene = Scene(root, 300.0, 250.0)
    primaryStage.show()
  }

  fun foo(args: List<String>) {
    launch()
  }
}
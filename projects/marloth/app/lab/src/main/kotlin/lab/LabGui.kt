package lab

import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.StackPane
import javafx.stage.Stage

class LabGui : Application() {

  override fun start(primaryStage: Stage) {
    primaryStage.title = "Hello World!"
    val btn = Button()
    btn.text = "Say 'Hello World'"
    btn.onAction = EventHandler { println("Hello World!") }

    val root = StackPane()
    root.children.add(btn)
    primaryStage.scene = Scene(root, 300.0, 250.0)
    primaryStage.show()
  }

  fun foo(args: List<String>) {
    launch()
  }
}
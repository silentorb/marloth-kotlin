package lab.gui

import getResourceUrl
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.util.Duration
import lab.gui.views.graphView
import lab.gui.views.textureList
import lab.mainAppClosed
import rendering.texturing.functions.newTextureEngine

fun periodicUpdate(gui: LabGui) {
  val updater = Timeline(KeyFrame(Duration.seconds(0.5), EventHandler {
    if (mainAppClosed)
      Platform.exit()
  }))
  updater.cycleCount = Timeline.INDEFINITE
  updater.play()
}

val textureLength = 256

class LabGui : Application() {

  override fun start(primaryStage: Stage) {
    primaryStage.title = "Texture Generation"

    periodicUpdate(this)
    val village = Village(
        engine = newTextureEngine(textureLength)
    )

    val root = BorderPane()
    var state = newState()
    var emit: Emitter? = null
    emit = { event ->
      val previousState = state
      state = updateState(village, state, event)
      if (state.graph != previousState.graph) {
        root.center = graphView(emit!!, village.engine, state)
      }
    }

    root.left = textureList(emit)
    root.center = graphView(emit, village.engine, state)

    val scene = Scene(root, 1200.0, 600.0)
    val s = getResourceUrl("style.css")
    scene.getStylesheets().add(s.toString())
    primaryStage.scene = scene
    primaryStage.show()
  }

  companion object {
    @JvmStatic
    fun mainMenu(args: List<String>) {
      Application.launch(LabGui::class.java)
    }
  }
}
package metaview

import configuration.loadYamlFile
import configuration.loadYamlResource
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.util.Duration
import metaview.views.graphView
import metaview.views.menuBarView
import metaview.views.textureList
import mythic.imaging.newTextureEngine
import java.io.File
import java.net.URL

fun getResourceUrl(path: String): URL {
  val classloader = Thread.currentThread().contextClassLoader
  return classloader.getResource(path)
}

fun periodicUpdate(gui: LabGui) {
  val updater = Timeline(KeyFrame(Duration.seconds(0.5), EventHandler {
    //    if (mainAppClosed)
//      Platform.exit()
  }))
  updater.cycleCount = Timeline.INDEFINITE
  updater.play()
}

val textureLength = 256

fun listProjectTextures(path: String): List<String> {
  return File(path).listFiles()
      .filter { it.extension == "json" }
      .map { it.nameWithoutExtension }
}

class LabGui : Application() {

  override fun start(primaryStage: Stage) {
    primaryStage.title = "Texture Generation"

    periodicUpdate(this)
    val village = Village(
        engine = newTextureEngine(textureLength)
    )

    val config = loadYamlFile<Config>("metaview.yaml")
    if (config == null)
      throw Error("Could not find required configuration file metaview.yaml")

    val textures = listProjectTextures(config.projectPath)

    val root = BorderPane()
    var state = refreshState(village, State(
        config = config,
        textureName = textures.firstOrNull()
    ))

    var emit: Emitter? = null
    val updateGraphView = { root.center = graphView(emit!!, village.engine, state) }
    emit = { event ->
      val previousState = state
      state = updateState(village, state, event)
      if (state.graph != previousState.graph || event.type == EventType.refresh) {
        updateGraphView()
      }
    }

    root.top = menuBarView(emit)
    root.left = textureList(emit, textures)
    updateGraphView()

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

object MetaView {
  @JvmStatic
  fun main(args: Array<String>) {
    LabGui.mainMenu(listOf())
  }
}
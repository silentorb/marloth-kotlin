package metaview

import configuration.loadYamlFile
import configuration.loadYamlResource
import configuration.saveJsonFile
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.stage.Window
import javafx.util.Duration
import metahub.OutputValues
import metahub.arrangeGraphStages
import metahub.execute
import metaview.views.graphView
import metaview.views.menuBarView
import metaview.views.propertiesView
import metaview.views.textureList
import mythic.ent.Id
import mythic.imaging.newTextureEngine
import java.io.File
import java.net.URL

fun getResourceUrl(path: String): URL {
  val classloader = Thread.currentThread().contextClassLoader
  return classloader.getResource(path)
}

private var _globalWindow: Window? = null

fun globalWindow() = _globalWindow!!

//fun periodicUpdate(gui: LabGui) {
//  val updater = Timeline(KeyFrame(Duration.seconds(0.5), EventHandler {
//    if (mainAppClosed)
//      Platform.exit()
//  }))
//  updater.cycleCount = Timeline.INDEFINITE
//  updater.play()
//}

val textureLength = 256

fun listProjectTextures(path: String): List<String> {
  return File(path).listFiles()
      .filter { it.extension == "json" }
      .map { it.nameWithoutExtension }
}

class LabGui : Application() {

  override fun start(primaryStage: Stage) {
    primaryStage.title = "Texture Generation"

//    periodicUpdate(this)
    val village = Village(
        engine = newTextureEngine(textureLength)
    )

    val config = loadYamlFile<Config>("metaview.yaml")
    if (config == null)
      throw Error("Could not find required configuration file metaview.yaml")

    val textures = listProjectTextures(config.projectPath)

    val root = BorderPane()
    val scene = Scene(root, 1200.0, 600.0)
    val s = getResourceUrl("style.css")
    scene.getStylesheets().add(s.toString())
    primaryStage.scene = scene
    _globalWindow = scene.window

    var state = State(
        config = config,
        textureName = textures.firstOrNull()
    )

    var emit: Emitter? = null
    val updateGraphView = { st: State, stages: List<List<Id>>, values: OutputValues ->
      root.center = graphView(emit!!, village.engine, st, stages, values)
    }

    val updatePropertiesView = { st: State, values: OutputValues ->
      root.right = propertiesView(emit!!, village.engine, st, values)
    }

    emit = { event ->
      val previousState = state
      val newState = updateState(village, state, event)
      val graph = newState.graph
      val (stages, values) = if (graph != null) {
        val _stages = arrangeGraphStages(graph)
        Pair(_stages, execute(village.engine, graph, _stages))
      } else
        Pair(listOf(), mapOf())
      updateGraphView(newState, stages, values)
      updatePropertiesView(newState, values)

      if (!event.preview) {
        state = newState
        if (state.textureName != null && state.graph != null && state.graph != previousState.graph && previousState.graph != null && state.textureName == previousState.textureName) {
          saveJsonFile(texturePath(state, state.textureName!!), state.graph!!)
        }
      }
    }

    root.top = menuBarView(emit)
    root.left = textureList(emit, textures)
    emit(Event(EventType.refresh))
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
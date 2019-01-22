package metaview

import configuration.loadYamlFile
import configuration.saveJsonFile
import configuration.saveYamlFile
import javafx.application.Application
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.stage.Window
import metahub.OutputValues
import metaview.views.*
import mythic.imaging.newTextureEngine
import java.awt.MouseInfo
import java.awt.Point
import java.io.File
import java.net.URL

fun getResourceUrl(path: String): URL {
  val classloader = Thread.currentThread().contextClassLoader
  return classloader.getResource(path)
}

private var _globalWindow: Window? = null

fun globalWindow() = _globalWindow!!

const val textureLength = 512

fun listProjectTextures(path: String): List<String> {
  return File(path).listFiles()
      .filter { it.extension == "json" }
      .map { it.nameWithoutExtension }
      .sorted()
}

fun newState(): State {
  val config = loadYamlFile<GuiState>("metaview.yaml")
  if (config == null)
    throw Error("Could not find required configuration file metaview.yaml")

  val textures = listProjectTextures(config.projectPath)
  return State(
      gui = config.copy(
          activeGraph = config.activeGraph ?: textures.firstOrNull()
      ),
      textures = textures
  )
}

fun saveConfig(config: GuiState) {
  saveYamlFile("metaview.yaml", config)
}

fun isOver(point: Point, node: Node): Boolean {
  val position = node.localToScene(0.0, 0.0)
  val bounds = node.boundsInLocal
  return point.x >= position.x
      && point.x < position.x + bounds.width
      && point.y >= position.y
      && point.y < position.y + bounds.height
}

fun getFocus(root: BorderPane): FocusContext {
  val screenMouse = MouseInfo.getPointerInfo().location
  val mouse = Point(screenMouse.x - globalWindow().x.toInt(), screenMouse.y - globalWindow().y.toInt())
  val isOver = { node: Node? ->
    if (node != null) isOver(mouse, node)
    else false
  }
  return when {
    isOver(root.left) -> FocusContext.graphs
    isOver(root.center) -> FocusContext.graph
    else -> FocusContext.none
  }
}

fun coreLogic(root: BorderPane, village: Village) {
  var state = newState()

  var emit: Emitter? = null

  val rightPanel = VBox()
  rightPanel.prefWidth = 400.0
  rightPanel.children.addAll(VBox(), VBox())

  root.right = rightPanel
  val graphContainer = ScrollPane()
  root.center = graphContainer

  val updateTextureListView = { st: State ->
    root.left = textureList(emit!!, st)
  }

  val updateGraphView = { st: State, values: OutputValues ->
    graphContainer.content = graphView(emit!!, st, values)
  }

  val updatePreviewView = { st: State, values: OutputValues ->
    rightPanel.children.set(0, previewView(emit!!, values)(st))
  }

  val updatePropertiesView = { st: State ->
    rightPanel.children.set(1, propertiesView(emit!!)(st))
  }

  emit = { event ->
    val previousState = state
    val focus = getFocus(root)
    val newState = updateState(village, getFocus(root), event)(state)
    val graph = newState.graph
    val values = if (graph != null)
      executeSanitized(village.engine, graph)
    else
      mapOf()
    updateGraphView(newState, values)
    updatePreviewView(newState, values)
    if (newState.textures.size != previousState.textures.size)
      updateTextureListView(newState)

    if (!event.preview) {
      state = newState
      if (state.graph != null && ((state.gui.activeGraph != null && state.gui.activeGraph == previousState.gui.activeGraph) || (state.graph != previousState.graph && previousState.graph != null))) {
        saveJsonFile(texturePath(state, state.gui.activeGraph!!), state.graph!!)
      }

      if (state.gui != previousState.gui) {
        saveConfig(state.gui)
      }

      if (state.gui.activeGraph != previousState.gui.activeGraph || state.gui.graphInteraction.nodeSelection != previousState.gui.graphInteraction.nodeSelection) {
        updatePropertiesView(state)
      }
    }
  }

  root.top = menuBarView(emit)
  updateTextureListView(state)

  listenForKeypresses(root, emit, { state })

  emit(Event(EventType.refresh))

  updatePropertiesView(state)
}

class LabGui : Application() {

  override fun start(primaryStage: Stage) {
    try {
      primaryStage.title = "Texture Generation"

//    periodicUpdate(this)
      val village = Village(
          engine = newTextureEngine(textureLength)
      )

      val root = BorderPane()
      val scene = Scene(root, 1400.0, 800.0)
      val s = getResourceUrl("style.css")
      scene.getStylesheets().add(s.toString())
      primaryStage.scene = scene
      _globalWindow = scene.window

      coreLogic(root, village)
      primaryStage.show()
    } catch (exception: Exception) {
      println(exception.stackTrace)
    }
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
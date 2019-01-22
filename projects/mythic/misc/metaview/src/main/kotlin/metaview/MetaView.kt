package metaview

import configuration.loadYamlFile
import configuration.saveJsonFile
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.stage.Window
import metahub.OutputValues
import metaview.views.*
import mythic.imaging.newTextureEngine
import java.net.URL

fun getResourceUrl(path: String): URL {
  val classloader = Thread.currentThread().contextClassLoader
  return classloader.getResource(path)
}

private var _globalWindow: Window? = null

fun globalWindow() = _globalWindow!!

const val textureLength = 512

fun newState(): State {
  val config = loadYamlFile<GuiState>("metaview.yaml")
  if (config == null)
    throw Error("Could not find required configuration file metaview.yaml")

  val textures = listProjectTextures(config.projectPath)
  return State(
      gui = config.copy(
          activeGraph = config.activeGraph ?: textures.firstOrNull()
      ),
      graphNames = textures
  )
}

fun coreLogic(root: BorderPane, village: Village) {
  var state = newState()

  var emit: Emitter? = null
  var history: List<State> = listOf()
  var future: List<State> = listOf()
  val maxHistory = 10

  val undo: StateTransform = { s ->
    if (history.size > 1) {
      future = history.takeLast(1).plus(future)
      history = history.dropLast(1)
      history.last()
    } else
      state
  }

  val redo: StateTransform = { s ->
    if (future.any()) {
      history = history.plus(future.first())
      future = future.drop(1)
      history.last()
    } else
      state
  }

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
    val newState = updateState(village, getFocus(root), event, undo, redo)(state)
    val graph = newState.graph
    val values = if (graph != null)
      executeSanitized(village.engine, graph)
    else
      mapOf()
    updateGraphView(newState, values)
    updatePreviewView(newState, values)
    if (newState.graphNames.size != previousState.graphNames.size)
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

      if (event.type != EventType.undo && event.type != EventType.redo && newState.graph != previousState.graph) {
        // maxHistory + 1 because the current state is stored in history, taking up one slot for a state that isn't actually a historical record
        // This way, when maxHistory is set to 10 then the user can actually undo 10 times instead of 9.
        history = history.plus(newState).take(maxHistory + 1)
        future = listOf() // Creating new history entries erases any possible forks
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
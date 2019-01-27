package metahub.metaview.front

import configuration.saveJsonFile
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.stage.Window
import metahub.core.Engine
import metahub.core.OutputValues
import metahub.metaview.common.*
import mythic.imaging.newTextureEngine
import java.net.URL

fun getResourceUrl(path: String): URL {
  val classloader = Thread.currentThread().contextClassLoader
  return classloader.getResource(path)
}

const val textureLength = 512

fun coreLogic(root: BorderPane, engine: Engine) {
  var state = newState()

  var emit: Emitter? = null
  var history: List<AppState> = listOf()
  var future: List<AppState> = listOf()
  val maxHistory = 10

  val undo: AppTransform = { s ->
    if (history.size > 1) {
      future = history.takeLast(1).plus(future)
      history = history.dropLast(1)
      history.last()
    } else
      state
  }

  val redo: AppTransform = { s ->
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

  val updateTextureListView = { st: CommonState ->
    root.left = textureList(emit!!, st)
  }

  val updateGraphView = { st: CommonState, values: OutputValues ->
    graphContainer.content = null // JavaFX has some weird caching/race condition that this prevents
    graphContainer.content = graphView(emit!!, st, values)
  }

  val updatePreviewView = { st: CommonState, values: OutputValues ->
    rightPanel.children.set(0, previewView(emit!!, values)(st))
  }

  val updatePropertiesView = { st: CommonState ->
    rightPanel.children.set(1, propertiesView(emit!!)(st))
  }

  val commonUpdater = updateCommonState(engine, nodeDefinitions, onNewGraph)
  val historyUpdater = updateHistory(undo, redo)

  emit = { event ->
    Platform.runLater {
      val previousState = state
      val focus = getFocus(root)
      val newState = updateAppState(engine, nodeDefinitions, commonUpdater, historyUpdater, getFocus(root), event)(state)
      val graph = newState.graph
      val values = if (graph != null)
        executeSanitized(engine, graph)
      else
        mapOf()
      updateGraphView(newState, values)
      updatePreviewView(newState, values)
      if (newState.graphNames.size != previousState.graphNames.size || newState.domain != previousState.domain)
        updateTextureListView(newState)

      if (!event.preview) {
        state = newState
        if (state.graph != null && ((state.gui.activeGraph != null && state.gui.activeGraph == previousState.gui.activeGraph) || (state.graph != previousState.graph && previousState.graph != null))) {
          saveJsonFile(texturePath(state, state.gui.activeGraph!!), state.graph!!)
        }

        if (state.gui != previousState.gui || state.texturing != previousState.texturing) {
          saveConfig(state)
        }

        if (state.gui.activeGraph != previousState.gui.activeGraph || state.gui.graphInteraction.nodeSelection != previousState.gui.graphInteraction.nodeSelection) {
          updatePropertiesView(state)
        }

        if (event.type != CommonEvent.undo && event.type != CommonEvent.redo && newState.graph != previousState.graph) {
          // maxHistory + 1 because the current state is stored in history, taking up one slot for a state that isn't actually a historical record
          // This way, when maxHistory is set to 10 then the user can actually undo 10 times instead of 9.
          history = history.plus(newState).take(maxHistory + 1)
          future = listOf() // Creating new history entries erases any possible forks
        }
      }
    }
  }

  root.top = VBox(5.0, menuBarView(emit), toolBarView(state, emit))
  updateTextureListView(state)

  listenForKeypresses(root, emit, { state })

  emit(Event(CommonEvent.refresh))

  updatePropertiesView(state)
}

class LabGui : Application() {

  override fun start(primaryStage: Stage) {
    try {
      primaryStage.title = "Texture Generation"

      val engine = newTextureEngine(textureLength)

      val root = BorderPane()
      val scene = Scene(root, 1400.0, 800.0)
      val s = getResourceUrl("style.css")
      scene.getStylesheets().add(s.toString())
      primaryStage.scene = scene
      _globalWindow = scene.window

      coreLogic(root, engine)
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
package metahub.metaview.front

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import metahub.core.Engine
import metahub.core.OutputValues
import metahub.metaview.common.*
import metahub.metaview.common.views.graphView
import metahub.metaview.common.views.menuBarView
import metahub.metaview.common.views.propertiesView
import metahub.metaview.common.views.textureList
import metahub.metaview.front.views.previewView
import metahub.metaview.front.views.toolBarView
import metahub.metaview.texturing.nodeDefinitions
import mythic.imaging.newTextureEngine
import java.net.URL

fun getResourceUrl(path: String): URL {
  val classloader = Thread.currentThread().contextClassLoader
  return classloader.getResource(path)
}

const val textureLength = 512

val connectableTypes = setOf(bitmapType, grayscaleType)

fun commonListener(listener: StateTransformListener<CommonState>): StateTransformListener<AppState> = { change ->
  { state ->
    val commonChange = StateTransformChange(
        previous = change.previous.common,
        event = change.event
    )
    state.copy(
        common = listener(commonChange)(state.common)
    )
  }
}

fun coreLogic(root: BorderPane, engine: Engine) {

  var history: List<AppState> = listOf()
  var future: List<AppState> = listOf()
  val maxHistory = 10

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
    graphContainer.content = graphView(engine, nodeDefinitions, connectableTypes, emit!!, st, values)
  }

  val updatePreviewView = { st: CommonState, values: OutputValues ->
    rightPanel.children.set(0, previewView(engine, nodeDefinitions, emit!!, values)(st))
  }

  val updatePropertiesView = { st: CommonState ->
    rightPanel.children.set(1, propertiesView(nodeDefinitions, engine, emit!!)(st))
  }

  val commonUpdater = updateCommonState(engine, nodeDefinitions, onNewGraph)
//  val historyUpdater = updateHistory(undo, redo)

  val transformListeners = listOf(
      commonListener(historyStateListener(10))
  )
  val emit = appLogic(transformListeners, listOf(), newState())
//  emit = { event ->
//    Platform.runLater {
//      val previousState = state
//      val focus = getFocus(root)
//      val newState = updateAppState(engine, nodeDefinitions, commonUpdater, historyUpdater, getFocus(root), event)(state)
//      val graph = newState.common.graph
//      val values = if (graph != null) {
//        val defaultValues = fillerTypeValues(textureLength)
//        executeSanitized(nodeDefinitions, defaultValues, engine, graph)
//      }
//      else
//        mapOf()
//      updateGraphView(newState.common, values)
//      updatePreviewView(newState.common, values)
//      if (newState.common.graphNames.size != previousState.common.graphNames.size || newState.domain != previousState.domain)
//        updateTextureListView(newState.common)
//
//      if (!event.preview) {
//        state = newState
//        if (state.common.graph != null && ((state.common.gui.activeGraph != null && state.common.gui.activeGraph == previousState.common.gui.activeGraph) || (state.common.graph != previousState.common.graph && previousState.common.graph != null))) {
//          saveJsonFile(texturePath(state.common, state.common.gui.activeGraph!!), state.common.graph!!)
//        }
//
//        if (state.common.gui != previousState.common.gui || state.texturing != previousState.texturing) {
//          saveConfig(state)
//        }
//
//        if (state.common.gui.activeGraph != previousState.common.gui.activeGraph || state.common.gui.graphInteraction.nodeSelection != previousState.common.gui.graphInteraction.nodeSelection) {
//          updatePropertiesView(state.common)
//        }
//
//        if (event.type != HistoryEvent.undo && event.type != HistoryEvent.redo && newState.common.graph != previousState.common.graph) {
//          // maxHistory + 1 because the current state is stored in history, taking up one slot for a state that isn't actually a historical record
//          // This way, when maxHistory is set to 10 then the user can actually undo 10 times instead of 9.
//          history = history.plus(newState).take(maxHistory + 1)
//          future = listOf() // Creating new history entries erases any possible forks
//        }
//      }
//    }
//  }

  root.top = VBox(5.0, menuBarView(emit), toolBarView(state, emit))
  updateTextureListView(state.common)

  listenForKeypresses(root, emit, { state })

  emit(Event(CommonEvent.refresh))

  updatePropertiesView(state.common)
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
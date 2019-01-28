package metahub.metaview.front

import configuration.saveJsonFile
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import metahub.core.Engine
import metahub.metaview.common.*
import metahub.metaview.common.views.*
import metahub.metaview.front.views.previewView
import metahub.metaview.front.views.toolBarView
import metahub.metaview.texturing.TexturingState
import metahub.metaview.texturing.fillerTypeValues
import metahub.metaview.texturing.nodeDefinitions
import metahub.metaview.texturing.texturingListener
import mythic.imaging.newTextureEngine
import java.net.URL

fun getResourceUrl(path: String): URL {
  val classloader = Thread.currentThread().contextClassLoader
  return classloader.getResource(path)
}

const val textureLength = 512

val connectableTypes = setOf(bitmapType, grayscaleType)

//fun commonListener(listener: StateTransformListener<CommonState>): StateTransformListener<AppState> = { change ->
//  { state ->
//    val commonChange = StateTransformChange(
//        previous = change.previous.common,
//        event = change.event
//    )
//    state.copy(
//        common = listener(commonChange)(state.common)
//    )
//  }
//}
//

//fun texturingListener(listener: StateTransformListener<TexturingState>): StateTransformListener<AppState> = { change ->
//  { state ->
//    val commonChange = StateTransformChange(
//        previous = change.previous.common,
//        event = change.event
//    )
//    state.copy(
//        common = listener(commonChange)(state.common)
//    )
//  }
//}

val commonListener = wrapListener<AppState, CommonState>({ it.common }) { a, b -> a.copy(common = b) }

val texturingWrapper = wrapListener<AppState, TexturingState>({ it.texturing }) { a, b -> a.copy(texturing = b) }

val configSaving: SideEffectStateListener<AppState> = { change ->
  val state = change.next
  val previousState = change.previous
  if (state.common.gui != previousState.common.gui || state.texturing != previousState.texturing) {
    saveConfig(state)
  }
}

fun graphSaving(): SideEffectStateListener<AppState> = { change ->
  val state = change.next
  val previousState = change.previous
  if (state.common.graph != null && ((state.common.gui.activeGraph != null && state.common.gui.activeGraph == previousState.common.gui.activeGraph) || (state.common.graph != previousState.common.graph && previousState.common.graph != null))) {
    saveJsonFile(texturePath(state.common, state.common.gui.activeGraph!!), state.common.graph!!)
  }
}

fun newValueDisplays(): ValueDisplayMap = mapOf(

)

fun coreLogic(root: BorderPane, engine: Engine) {
  val rightPanel = VBox()
  rightPanel.prefWidth = 400.0
  rightPanel.children.addAll(VBox(), VBox())

  root.right = rightPanel
  val graphContainer = ScrollPane()
  root.center = graphContainer

  val transformListeners: List<StateTransformListener<AppState>> = listOf<StateTransformListener<AppState>>(
      domainListener(engine, nodeDefinitions),
      texturingWrapper(texturingListener)
  )
      .plus(listOf<StateTransformListener<CommonState>>(
          commonStateListener(engine, nodeDefinitions) { getFocus(root)},
          stateTransformListener(graphTransform(onNewGraph)),
          onGraphChanged(nodeDefinitions, fillerTypeValues(textureLength), engine),
          historyStateListener(10)
      )
          .map(commonListener))

  val sideEffectListeners: MutableList<SideEffectStateListener<AppState>> = mutableListOf()
  val valueDisplays = newValueDisplays()
  val initialState = newState()

  val (emit, getState) = appLogic(transformListeners, sideEffectListeners, initialState)

  root.left = textureList(emit, initialState.common)
  rightPanel.children.set(1, propertiesView(nodeDefinitions, engine, emit)(initialState.common))
  graphContainer.content = graphView(engine, nodeDefinitions, connectableTypes, valueDisplays, emit, initialState.common)

  val updateTextureListView: SideEffectStateListener<AppState> = { change ->
    val previousState = change.previous
    val newState = change.next
    if (!change.event.preview && newState.common.graphNames.size != previousState.common.graphNames.size || newState.domain != previousState.domain) {
      root.left = textureList(emit, change.next.common)
    }
  }

  val updateGraphView: SideEffectStateListener<AppState> = { change ->
    if (change.next.common.graph != change.previous.common.graph) {
      graphContainer.content = null // JavaFX has some weird caching/race condition that this prevents
      graphContainer.content = graphView(engine, nodeDefinitions, connectableTypes, valueDisplays, emit, change.next.common)
    }
  }

  val updatePreviewView: SideEffectStateListener<AppState> = { change ->
    rightPanel.children.set(0, previewView(engine, nodeDefinitions, valueDisplays, emit)(change.next.common))
  }

  val updatePropertiesView: SideEffectStateListener<AppState> = { change ->
    val state = change.next.common
    val previousState = change.previous.common
    if (!change.event.preview && state.gui.activeGraph != previousState.gui.activeGraph || state.gui.graphInteraction.nodeSelection != previousState.gui.graphInteraction.nodeSelection) {
      rightPanel.children.set(1, propertiesView(nodeDefinitions, engine, emit)(state))
    }
  }

  sideEffectListeners.plus(listOf(
      updateTextureListView,
      updateGraphView,
      updatePreviewView,
      updatePropertiesView,
      configSaving,
      graphSaving()
  ))

  root.top = VBox(5.0, menuBarView(emit), toolBarView(initialState, emit))

  listenForKeypresses(root, emit, { getState().common })

  emit(Event(CommonEvent.refresh))

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
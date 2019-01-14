package metaview

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import metahub.*
import mythic.ent.*
import java.nio.file.Files
import java.nio.file.Paths

typealias StateTransform = (State) -> State

enum class FocusContext {
  graph,
  graphs,
  none
}

fun texturePath(state: State, name: String): String =
    "${state.config.projectPath}/$name.json"

fun loadTextureGraph(engine: Engine, state: State, name: String): Graph =
    loadGraphFromFile(engine, texturePath(state, name))

fun selectTexture(village: Village, name: String): StateTransform = { state ->
  state.copy(
      textureName = name,
      graph = loadTextureGraph(village.engine, state, name),
      graphInteraction = GraphInteraction()
  )
}

fun refreshState(village: Village): StateTransform = { state ->
  state.copy(
      graph = if (state.textureName != null)
        loadTextureGraph(village.engine, state, state.textureName)
      else
        null
  )
}

fun selectNode(id: Id): StateTransform = { state ->
  val reselecting = state.graphInteraction.nodeSelection.contains(id)
  if (state.graphInteraction.mode == GraphMode.connecting)
    if (reselecting)
      state
    else
      state.copy(
          graphInteraction = state.graphInteraction.copy(
              mode = GraphMode.normal
          ),
          graph = newConnection(id, state.graphInteraction.portSelection.first())(state.graph!!)
      )
  else
    state.copy(
        graphInteraction = if (reselecting)
          state.graphInteraction.copy(nodeSelection = state.graphInteraction.nodeSelection.minus(id))
        else
          GraphInteraction(nodeSelection = listOf(id))
    )
}

fun selectInput(port: Port): StateTransform = { state ->
  val newSelection = if (state.graphInteraction.portSelection.contains(port))
    state.graphInteraction.copy(portSelection = state.graphInteraction.portSelection.minus(port))
  else
    GraphInteraction(portSelection = listOf(port)).copy()

  state.copy(
      graphInteraction = newSelection.copy(
          mode = GraphMode.normal
      )
  )
}

fun changeInputValue(change: InputValue): StateTransform = { state ->
  val graph = state.graph!!
  val newValues = replace(graph.values, change, ::isSameInput)
  state.copy(
      graph = graph.copy(
          values = newValues
      )
  )
}

fun renameTexture(change: Renaming): StateTransform = { state ->
  Files.move(
      Paths.get(texturePath(state, change.previousName)),
      Paths.get(texturePath(state, change.newName))
  )
  state.copy(
      textureName = change.newName,
      textures = state.textures.map {
        if (it == change.previousName)
          change.newName
        else
          it
      }
  )
}

fun newTexture(name: String): StateTransform = { state ->
  state.copy(
      textures = state.textures.plus(name).sorted(),
      textureName = name,
      graph = Graph()
  )
}

fun addNode(name: String): StateTransform = { state ->
  if (state.graph == null)
    state
  else {
    val definition = nodeDefinitions[name]!!
    val values = definition.inputs.mapNotNull {
      val defaultValue = it.value.defaultValue
      if (defaultValue != null)
        Pair(it.key, defaultValue)
      else
        null
    }
        .associate { it }

    state.copy(
        graph = newNode(name, values)(state.graph),
        graphInteraction = GraphInteraction()
    )
  }
}

val deleteGraphSelection: StateTransform = { state ->
  val selection = state.graphInteraction
  state.copy(
      graph = transformNotNull(state.graph, pipe(deleteNodes(selection.nodeSelection), deleteConnection(selection.portSelection))),
      graphInteraction = GraphInteraction()
  )
}

fun confirmFileDeletion(name: String): Boolean {
  val dialog = Alert(Alert.AlertType.CONFIRMATION)
  val filename = "$name.json"
  dialog.title = "Deleting $filename"
  dialog.contentText = "Are you sure you want to delete $filename?"
  dialog.headerText = null
  dialog.graphic = null
  val result = dialog.showAndWait()
  return !result.isEmpty
      && result.get() == ButtonType.OK
}

val deleteGraph: StateTransform = { state ->
  val name = state.textureName
  if (name != null && confirmFileDeletion(name)) {
    Files.delete(Paths.get(texturePath(state, name)))
    val index = state.textures.indexOf(name)
    val newTextures = state.textures.minus(name)
    state.copy(
        textures = newTextures,
        textureName = state.textures.getOrNull(index) ?: state.textures.firstOrNull()
    )
  } else
    state
}

fun deleteSelected(focus: FocusContext): StateTransform =
    when (focus) {
      FocusContext.graph -> deleteGraphSelection
      FocusContext.graphs -> deleteGraph
      else -> ::pass
    }

val startConnection: StateTransform = { state ->
  if (state.graphInteraction.portSelection.none())
    state
  else
    state.copy(
        graphInteraction = state.graphInteraction.copy(
            mode = GraphMode.connecting
        )
    )
}

fun onConnecting(focus: FocusContext): StateTransform =
    when (focus) {
      FocusContext.graph -> startConnection
      else -> ::pass
    }

fun updateState(village: Village, focus: FocusContext, state: State, event: Event): State {
  val transform = when (event.type) {
    EventType.addNode -> addNode(event.data as String)
    EventType.deleteSelected -> deleteSelected(focus)
    EventType.connecting -> onConnecting(focus)
    EventType.inputValueChanged -> changeInputValue(event.data as InputValue)
    EventType.textureSelect -> selectTexture(village, event.data as String)
    EventType.newTexture -> newTexture(event.data as String)
    EventType.selectInput -> selectInput(event.data as Port)
    EventType.selectNode -> selectNode(event.data as Id)
    EventType.refresh -> refreshState(village)
    EventType.renameTexture -> renameTexture(event.data as Renaming)
  }

  return transform(state)
}

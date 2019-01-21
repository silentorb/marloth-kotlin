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

fun guiTransform(transform: (GuiState) -> GuiState): StateTransform = { state ->
  state.copy(
      gui = transform(state.gui)
  )
}

fun activeGraphChanged(name: String?): StateTransform = { state ->
  state.copy(
      gui = state.gui.copy(
          activeGraph = name,
          graphInteraction = GraphInteraction()
      )
  )
}

fun texturePath(state: State, name: String): String =
    "${state.gui.projectPath}/$name.json"

fun loadTextureGraph(engine: Engine, state: State, name: String): Graph =
    loadGraphFromFile(engine, texturePath(state, name))

fun selectTexture(village: Village, name: String): StateTransform = pipe({ state: State ->
  state.copy(
      graph = loadTextureGraph(village.engine, state, name)
  )
}, activeGraphChanged(name))

fun refreshState(village: Village): StateTransform = { state ->
  state.copy(
      graph = if (state.gui.activeGraph != null)
        loadTextureGraph(village.engine, state, state.gui.activeGraph)
      else
        null
  )
}

fun isReselecting(id: Id, state: State): Boolean =
    state.gui.graphInteraction.nodeSelection.contains(id)

fun connectNodes(id: Id): StateTransform = { state ->
  if (isReselecting(id, state))
    state
  else {
    val port = state.gui.graphInteraction.portSelection.first()
    val newGraph = if (port.node == 0L)
      setOutput(id, port.input)
    else
      newConnection(id, port)

    state.copy(
        gui = state.gui.copy(
            graphInteraction = state.gui.graphInteraction.copy(
                mode = GraphMode.normal
            )
        ),
        graph = newGraph(state.graph!!)
    )
  }
}

fun toggleNodeSelection(id: Id): StateTransform = { state ->
  state.copy(
      gui = state.gui.copy(
          graphInteraction = if (isReselecting(id, state))
            state.gui.graphInteraction.copy(nodeSelection = state.gui.graphInteraction.nodeSelection.minus(id))
          else
            GraphInteraction(nodeSelection = listOf(id))
      )
  )
}

fun selectNode(id: Id): StateTransform = { state ->
  if (state.gui.graphInteraction.mode == GraphMode.connecting)
    connectNodes(id)(state)
  else
    toggleNodeSelection(id)(state)
}

fun selectInput(port: Port): StateTransform = { state ->
  val newSelection = if (state.gui.graphInteraction.portSelection.contains(port))
    state.gui.graphInteraction.copy(portSelection = state.gui.graphInteraction.portSelection.minus(port))
  else
    GraphInteraction(portSelection = listOf(port)).copy()

  state.copy(
      gui = state.gui.copy(
          graphInteraction = newSelection.copy(
              mode = GraphMode.normal
          )
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
      gui = state.gui.copy(
          activeGraph = change.newName
      ),
      textures = state.textures.map {
        if (it == change.previousName)
          change.newName
        else
          it
      }
  )
}

fun newTexture(name: String): StateTransform = pipe({ state ->
  state.copy(
      textures = state.textures.plus(name).sorted(),
      graph = Graph()
  )
}, activeGraphChanged(name))

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
        gui = state.gui.copy(
            graphInteraction = GraphInteraction()
        )
    )
  }
}


fun deleteConnectionsOrOutputConnections(ports: List<Port>): GraphTransform {
  val (outputConnections, connections) = ports.partition { it.node == 0L }
  return pipe(deleteConnections(connections), deleteOutputConnections(outputConnections))
}

val deleteGraphSelection: StateTransform = { state ->
  val selection = state.gui.graphInteraction
  val newGraph = transformNotNull(state.graph, pipe(
      deleteNodes(selection.nodeSelection),
      deleteConnectionsOrOutputConnections(selection.portSelection)
  ))

  state.copy(
      graph = newGraph,
      gui = state.gui.copy(
          graphInteraction = GraphInteraction()
      )
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
  val name = state.gui.activeGraph
  if (name != null && confirmFileDeletion(name)) {
    Files.delete(Paths.get(texturePath(state, name)))
    val index = state.textures.indexOf(name)
    val newTextures = state.textures.minus(name)
    val newName = state.textures.getOrNull(index) ?: state.textures.firstOrNull()

    pipe(state.copy(
        textures = newTextures
    ), listOf(activeGraphChanged(newName)))
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
  if (state.gui.graphInteraction.portSelection.none())
    state
  else
    state.copy(
        gui = state.gui.copy(
            graphInteraction = state.gui.graphInteraction.copy(
                mode = GraphMode.connecting
            )
        )
    )
}

fun onConnecting(focus: FocusContext): StateTransform =
    when (focus) {
      FocusContext.graph -> startConnection
      else -> ::pass
    }

fun setTilePreview(value: Boolean): StateTransform = guiTransform { gui ->
  gui.copy(
      tilePreview = value
  )
}

fun updateState(village: Village, focus: FocusContext, event: Event): StateTransform =
    when (event.type) {
      EventType.addNode -> addNode(event.data as String)
      EventType.deleteSelected -> deleteSelected(focus)
      EventType.connecting -> onConnecting(focus)
      EventType.inputValueChanged -> changeInputValue(event.data as InputValue)
      EventType.textureSelect -> selectTexture(village, event.data as String)
      EventType.newTexture -> newTexture(event.data as String)
      EventType.selectInput -> selectInput(event.data as Port)
      EventType.selectNode -> selectNode(event.data as Id)
      EventType.setTilePreview -> setTilePreview(event.data as Boolean)
      EventType.refresh -> refreshState(village)
      EventType.renameTexture -> renameTexture(event.data as Renaming)
    }
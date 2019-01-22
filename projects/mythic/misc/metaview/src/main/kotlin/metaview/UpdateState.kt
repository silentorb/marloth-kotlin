package metaview

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import metahub.*
import metaview.views.getDefinition
import mythic.ent.*
import mythic.imaging.textureOutputTypes
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

fun graphTransform(transform: GraphTransform): StateTransform = { state ->
  state.copy(
      graph = transformNotNull(state.graph, transform)
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
  val graph = loadTextureGraph(village.engine, state, name)
  val gui = state.gui
  state.copy(
      graph = graph,
      gui = gui.copy(
          graphInteraction = gui.graphInteraction.copy(
              nodeSelection = gui.graphInteraction.nodeSelection.filter { graph.nodes.contains(it) },
              portSelection = gui.graphInteraction.portSelection.filter { graph.nodes.contains(it.node) }
          )
      )
  )
}, activeGraphChanged(name))

fun refreshState(village: Village): StateTransform = { state ->
  if (state.gui.activeGraph != null)
    selectTexture(village, state.gui.activeGraph)(state)
  else
    state
}

fun isReselecting(id: Id, state: State): Boolean =
    state.gui.graphInteraction.nodeSelection.contains(id)

val isOutputNode = isOutputNode(textureOutputTypes)

fun connectNodes(id: Id): StateTransform = { state ->
  if (isReselecting(id, state) || isOutputNode(state.graph!!, id))
    state
  else {
    val port = state.gui.graphInteraction.portSelection.first()
    val newGraph =
//        if (port.node == 0L)
//      setOutput(id, port.input)
//    else
        newConnection(id, port)

    state.copy(
        gui = state.gui.copy(
            graphInteraction = state.gui.graphInteraction.copy(
                mode = GraphMode.normal
            )
        ),
        graph = newGraph(state.graph)
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
      graphNames = state.graphNames.map {
        if (it == change.previousName)
          change.newName
        else
          it
      }
  )
}

fun newGraph(name: String): StateTransform = pipe({ state ->
  state.copy(
      graphNames = state.graphNames.plus(name).sorted(),
      graph = Graph(
          nodes = setOf(1L),
          functions = mapOf(1L to textureOutput)
      )
  )
}, activeGraphChanged(name))

fun newNodeWithDefaults(name: String, id: Id): GraphTransform {
  val definition = nodeDefinitions[name]!!
  val values = definition.inputs.mapNotNull {
    val defaultValue = it.value.defaultValue
    if (defaultValue != null)
      Pair(it.key, defaultValue)
    else
      null
  }
      .associate { it }

  return newNode(name, values, id)
}

fun newNode(name: String): StateTransform = { state ->
  val id = nextNodeId(state.graph!!)
  state.copy(
      graph = newNodeWithDefaults(name, id)(state.graph),
      gui = state.gui.copy(
          graphInteraction = GraphInteraction(
              nodeSelection = listOf(id)
          )
      )
  )
}

fun addNode(name: String): StateTransform = { state ->
  newNode(name)(state)
}

//fun getPossibleInput(graph: Graph, inputNode: Id, outputNode: Id): String? {
//  val inputDefinition = getDefinition(graph, inputNode)
//  val outputDefinition = getDefinition(graph, outputNode)
//  val input = outputDefinition.inputs.entries.firstOrNull { it.value.type == inputDefinition.outputType }
//  return input?.key
//}

fun insertNode(name: String): StateTransform = pipe(
    { state ->
      val graph = state.graph!!
      val port = state.gui.graphInteraction.portSelection.first()
      val middleNode = nextNodeId(graph)
      val existingConnection = getConnection(graph, port)
      val additional = if (existingConnection != null) {
        val deletion = deleteConnections(listOf(Port(existingConnection.output, existingConnection.port)))
        val inputNode = existingConnection.input
        val inputDefinition = getDefinition(graph, inputNode)
        val outputDefinition = nodeDefinitions[name]!!
        val input = outputDefinition.inputs.entries
            .firstOrNull { it.value.type == inputDefinition.outputType }?.key
        if (input != null)
          pipe(
              newConnection(existingConnection.input, Port(middleNode, input)),
              deletion
          )
        else
          deletion
      } else
        ::pass

      val changes = pipe(
          newConnection(middleNode, port),
          additional
      )

      state.copy(
          graph = changes(graph)
      )
    },
    newNode(name)
)

//fun deleteConnectionsOrOutputConnections(ports: List<Port>): GraphTransform {
//  val (outputConnections, connections) = ports.partition { it.node == 0L }
//  return pipe(deleteConnections(connections), deleteOutputConnections(outputConnections))
//}

val deleteGraphSelection: StateTransform = { state ->
  val selection = state.gui.graphInteraction
  val newGraph = transformNotNull(state.graph, pipe(
      deleteNodes(selection.nodeSelection.filter { !isOutputNode(state.graph!!, it) }),
      deleteConnections(selection.portSelection)
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
    val index = state.graphNames.indexOf(name)
    val newTextures = state.graphNames.minus(name)
    val newName = state.graphNames.getOrNull(index) ?: state.graphNames.firstOrNull()

    pipe(state.copy(
        graphNames = newTextures
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
      EventType.insertNode -> insertNode(event.data as String)
      EventType.textureSelect -> selectTexture(village, event.data as String)
      EventType.newTexture -> newGraph(event.data as String)
      EventType.selectInput -> selectInput(event.data as Port)
      EventType.selectNode -> selectNode(event.data as Id)
      EventType.setTilePreview -> setTilePreview(event.data as Boolean)
      EventType.refresh -> refreshState(village)
      EventType.renameTexture -> renameTexture(event.data as Renaming)
    }
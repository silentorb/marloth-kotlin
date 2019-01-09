package metaview

import metahub.Engine
import metahub.Graph
import metahub.loadGraphFromFile
import mythic.ent.Id
import java.nio.file.Files
import java.nio.file.Paths

typealias StateTransform = (State) -> State

fun texturePath(state: State, name: String): String =
    "${state.config.projectPath}/$name.json"

fun loadTextureGraph(engine: Engine, state: State, name: String): Graph =
    loadGraphFromFile(engine, texturePath(state, name))

fun selectTexture(village: Village, name: String): StateTransform = { state ->
  state.copy(
      textureName = name,
      graph = loadTextureGraph(village.engine, state, name),
      nodeSelection = listOf()
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
  state.copy(
      nodeSelection = if (state.nodeSelection.contains(id))
        state.nodeSelection.minus(id)
      else
        listOf(id)
  )
}

fun changeInputValue(change: InputValueChange): StateTransform = { state ->
  val graph = state.graph!!
  val nodeValues = (graph.values[change.node] ?: mapOf())
      .plus(change.input to change.value)

  state.copy(
      graph = graph.copy(
          values = graph.values.plus(
              change.node to nodeValues
          )
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
      textureName = name ,
      graph = Graph()
  )
}

fun updateState(village: Village, state: State, event: Event): State {
  val transform = when (event.type) {
    EventType.inputValueChanged -> changeInputValue(event.data as InputValueChange)
    EventType.textureSelect -> selectTexture(village, event.data as String)
    EventType.newTexture -> newTexture(event.data as String)
    EventType.nodeSelect -> selectNode(event.data as Id)
    EventType.refresh -> refreshState(village)
    EventType.renameTexture -> renameTexture(event.data as Renaming)
  }

  return transform(state)
}

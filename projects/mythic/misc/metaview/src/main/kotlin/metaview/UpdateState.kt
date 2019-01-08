package metaview

import metahub.Engine
import metahub.loadGraphFromFile

fun loadTextureGraph(engine: Engine, state: State, name: String) =
    loadGraphFromFile(engine, "${state.config.projectPath}/$name.json")

fun selectTexture(village: Village, state: State, name: String): State {
  return state.copy(
      textureName = name,
      graph = loadTextureGraph(village.engine, state, name)
  )
}

fun refreshState(village: Village, state: State): State {
  return state.copy(
      graph = if (state.textureName != null)
        loadTextureGraph(village.engine, state, state.textureName)
      else
        null
  )
}

fun updateState(village: Village, state: State, event: Event): State {
  return when (event.type) {
    EventType.textureSelect -> selectTexture(village, state, event.data as String)
    EventType.refresh -> refreshState(village, state)
  }
}

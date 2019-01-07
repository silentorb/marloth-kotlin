package lab.gui

import metahub.Engine
import rendering.texturing.loadTextureGraph

fun loadTextureGraph2(engine: Engine, name: String) =
    loadTextureGraph(engine, "procedural/textures/$name.json")

fun selectTexture(village: Village, state: State, name: String): State {
  return state.copy(
      textureName = name,
      graph = loadTextureGraph2(village.engine, name)
  )
}

fun refreshState(village: Village, state: State): State {
  return state.copy(
      graph = if (state.textureName != null)
        loadTextureGraph2(village.engine, state.textureName)
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

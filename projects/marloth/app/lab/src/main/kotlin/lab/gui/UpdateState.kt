package lab.gui

import rendering.texturing.loadTextureGraph

fun selectTexture(village: Village, state: State, name: String): State {
  return state.copy(
      textureName = name,
      graph = loadTextureGraph(village.engine, "procedural/textures/$name.json")
  )
}

fun updateState(village: Village, state: State, event: Event): State {
  return when (event.type) {
    EventType.textureSelect -> selectTexture(village, state, event.data as String)
  }
}

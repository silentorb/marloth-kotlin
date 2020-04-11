package marloth.clienting

import silentorb.mythic.spatial.zw
import silentorb.mythic.lookinglass.*
import silentorb.mythic.lookinglass.drawing.renderBackground

fun prepareRender(renderer: GameSceneRenderer): List<ScreenFilter> {
  val r = renderer.renderer.renderer
  val filters = getDisplayConfigFilters(r.config).plus(renderer.scene.filters)
  renderer.prepareRender(filters)
  return filters
}

fun renderScene(renderer: GameSceneRenderer) {
  val scene = renderer.scene
  renderBackground(renderer.renderer.renderer, renderer.renderer.camera, scene.background)
  renderElements(renderer.renderer, scene.opaqueElementGroups, scene.transparentElementGroups)
  if (false) {
    renderArmatures(renderer)
  }
}

fun finishRender(renderer: GameSceneRenderer, filters: List<ScreenFilter>) {
  renderer.finishRender(renderer.renderer.viewport.zw, filters)
}

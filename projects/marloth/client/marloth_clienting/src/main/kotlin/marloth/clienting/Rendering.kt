package marloth.clienting

import mythic.drawing.Canvas
import mythic.platforming.WindowInfo
import org.joml.zw
import rendering.*
import rendering.drawing.renderBackground


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

//fun renderScenesOld(client: Client, scenes: List<GameScene>) {
//  val windowInfo = client.getWindowInfo()
//  val renderer = client.renderer
//  renderer.prepareRender(windowInfo)
//
//  val viewports = getPlayerViewports(scenes.size, windowInfo.dimensions).iterator()
//  for (scene in scenes) {
//    val viewport = viewports.next()
//    val sceneRenderer = renderer.createSceneRenderer(scene.main, viewport)
//    val gameRenderer = GameSceneRenderer(scene, sceneRenderer)
//    renderScene(gameRenderer)
//
////    val canvas = createCanvas(client.renderer, windowInfo)
////    if (config.draw.gui)
////      renderGui(sceneRenderer, Bounds(viewport.toVector4()), canvas, data.menuState)
//  }
//
//  renderer.finishRender(windowInfo)
//}

fun <T> renderContainer(client: Client, windowInfo: WindowInfo, action: (Canvas) -> T): T {
  val renderer = client.renderer
  renderer.prepareRender(windowInfo)
  val canvas = createCanvas(client.renderer, windowInfo)

  val result = action(canvas)

  renderer.finishRender(windowInfo)
  return result
}

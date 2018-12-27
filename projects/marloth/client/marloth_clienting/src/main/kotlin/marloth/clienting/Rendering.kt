package marloth.clienting

import mythic.drawing.Canvas
import mythic.glowing.globalState
import mythic.platforming.WindowInfo
import org.joml.zw
import rendering.*


fun renderScene(renderer: GameSceneRenderer) {
  val r = renderer.renderer.renderer
  val filters = getDisplayConfigFilters(r.config).plus(renderer.scene.filters)
  renderer.prepareRender(filters)
  renderer.renderWorldMesh()
  renderer.renderElements()

//  renderSkyBox(r.mappedTextures, r.meshes, r.shaders)
  renderer.finishRender(renderer.renderer.viewport.zw, filters)
  globalState.cullFaces = false

//  renderer.prepareRender()
//  renderer.renderElements()
//
//  val r = renderer.renderer.renderer
////  renderSkyBox(r.mappedTextures, r.meshes, r.shaders)
//  renderer.finishRender(renderer.renderer.viewport.zw)
//  globalState.cullFaces = false
}

fun renderScenesOld(client: Client, scenes: List<GameScene>) {
  val windowInfo = client.getWindowInfo()
  val renderer = client.renderer
  renderer.prepareRender(windowInfo)

  val viewports = getPlayerViewports(scenes.size, windowInfo.dimensions).iterator()
  for (scene in scenes) {
    val viewport = viewports.next()
    val sceneRenderer = renderer.createSceneRenderer(scene.main, viewport)
    val gameRenderer = GameSceneRenderer(scene, sceneRenderer)
    renderScene(gameRenderer)

//    val canvas = createCanvas(client.renderer, windowInfo)
//    if (config.draw.gui)
//      renderGui(sceneRenderer, Bounds(viewport.toVector4()), canvas, data.menuState)
  }

  renderer.finishRender(windowInfo)
}

fun <T> renderContainer(client: Client, windowInfo: WindowInfo, action: (Canvas) -> T): T {
  val renderer = client.renderer
  renderer.prepareRender(windowInfo)
  val canvas = createCanvas(client.renderer, windowInfo)

  val result = action(canvas)

  renderer.finishRender(windowInfo)
  return result
}
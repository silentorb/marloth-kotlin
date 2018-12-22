package marloth.clienting

import marloth.clienting.gui.renderGui
import mythic.bloom.Bounds
import mythic.drawing.Canvas
import mythic.glowing.globalState
import mythic.platforming.WindowInfo
import org.joml.Vector4i
import org.joml.zw
import rendering.*
import simulation.World


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

fun renderContainer(client: Client, action: (Canvas, WindowInfo) -> Unit) {
  val windowInfo = client.getWindowInfo()
  val renderer = client.renderer
  renderer.prepareRender(windowInfo)
  val canvas = createCanvas(client.renderer, windowInfo)

  action(canvas, windowInfo)

  renderer.finishRender(windowInfo)
}

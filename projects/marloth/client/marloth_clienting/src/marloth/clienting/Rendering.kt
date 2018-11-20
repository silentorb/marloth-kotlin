package marloth.clienting

import mythic.glowing.globalState
import org.joml.zw
import rendering.*


fun renderScene(renderer: GameSceneRenderer) {
  renderer.prepareRender()
  renderer.renderElements()

  val r = renderer.renderer.renderer
//  renderSkyBox(r.mappedTextures, r.meshes, r.shaders)
  renderer.finishRender(renderer.renderer.viewport.zw)
  globalState.cullFaces = false
}

fun renderScenes(client: Client, scenes: List<GameScene>) {
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
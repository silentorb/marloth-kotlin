package lab.views.map

import lab.views.GameDisplayMode
import lab.views.GameViewRenderData
import lab.views.renderFaceNormals
import lab.views.renderWireframeScene
import marloth.clienting.Client
import marloth.clienting.gui.renderGui
import mythic.bloom.Bounds
import mythic.spatial.toVector4
import rendering.GameSceneRenderer
import rendering.createCanvas
import rendering.getPlayerViewports
import simulation.AbstractWorld


fun renderMapView(client: Client, world: AbstractWorld, config: MapViewConfig) {
  val windowInfo = client.getWindowInfo()
  val renderer = client.renderer
  renderer.prepareRender(windowInfo)

  val canvas = createCanvas(client.renderer, windowInfo)

//  val viewports = getPlayerViewports(data.scenes.size, windowInfo.dimensions).iterator()
//  for (scene in data.scenes) {
//    val viewport = viewports.next()
//    val sceneRenderer = renderer.createSceneRenderer(scene.main, viewport)
//    val gameRenderer = GameSceneRenderer(scene, sceneRenderer)
//    when (data.config.displayMode) {
//      GameDisplayMode.normal -> gameRenderer.render()
//      GameDisplayMode.wireframe -> renderWireframeScene(gameRenderer, data)
//    }
//
//    if (data.config.drawNormals)
//      renderFaceNormals(sceneRenderer, 1f, data.world.mesh)
//
//    renderGui(sceneRenderer, Bounds(viewport.toVector4()), canvas, data.menuState)
//  }

  renderer.finishRender(windowInfo)
}
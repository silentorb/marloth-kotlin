package marloth.integration

import marloth.clienting.*
import marloth.front.RenderHook
import mythic.bloom.next.Box
import mythic.bloom.renderLayout
import mythic.platforming.WindowInfo
import rendering.*
import scenery.Light
import simulation.misc.interpolateWorlds

//val getMarchedBuffers = singleCache(::newMarchBuffers)

//fun renderRayMarching(gameScene: GameScene, dimensions: Vector2i, world: World, renderer: Renderer, marcher: Marcher) {
//  throw Error("Not implemented")
//  val buffers = getMarchedBuffers(dimensions.x * dimensions.y)
//  val scene = Scene(
//      camera = Camera(
//          position = gameScene.camera.position,
//          orientation = gameScene.camera.orientation,
//          near = 0.01f,
//          far = 1000f
//      ),
//      sdf = prepareSceneSdf(world.players.first().id, world),
//      lights = listOf()
//  )
//  val cast = perspectiveRay(scene.camera)
//
//  renderToMarchBuffers(buffers, marcher, scene, cast, dimensions)
////  val mix = postPipeline(dimensions, buffers)
////  rgbFloatToBytes(mix, renderer.renderColor.buffer!!)
////  normalizeDepthBuffer(0.01f, 1000f, buffers.depth, renderer.renderDepth.buffer!!)
////  renderer.applyRenderBuffer(dimensions)
//}

fun renderMain(client: Client, windowInfo: WindowInfo, appState: AppState, box: Box, onRender: RenderHook?) {
  client.platform.display.swapBuffers()

//  val marcher = Marcher(
//      end = 100f,
//      maxSteps = 100
//  )

  renderContainer(client, windowInfo) { canvas ->
    val world = interpolateWorlds(appState.timestep.accumulator, appState.worlds)
    if (world != null) {
      val scenes = createScenes(world.deck, client.screens)
      val dimensions = windowInfo.dimensions
      val viewports = getPlayerViewports(scenes.size, dimensions).iterator()
      val scene = scenes.first()
//      for (s in scenes) {
      val sceneRenderer = createSceneRenderer(client.renderer, scene, viewports.next())
      val gameRenderer = GameSceneRenderer(scene, sceneRenderer)
      val filters = prepareRender(gameRenderer)

      if (true) {
        renderScene(gameRenderer)
      }

//      if (false) {
//        renderRayMarching(scene, dimensions, world, client.renderer, marcher)
//      }

      if (onRender != null) {
        onRender(sceneRenderer)
      }

      finishRender(gameRenderer, filters)
//      }
    }

//      val bounds = Bounds(Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y))
    renderLayout(box, canvas)
//      renderGui(client, bounds, canvas, world, appState.client)
  }
}

package marloth.integration

import marloth.clienting.*
import mythic.bloom.Boxes
import mythic.bloom.renderLayout
import mythic.ent.singleCache
import mythic.imaging.rgbFloatToBytes
import mythic.platforming.WindowInfo
import org.joml.Vector2i
import rendering.GameScene
import rendering.GameSceneRenderer
import rendering.Renderer
import rendering.getPlayerViewports
import silentorb.raymarching.*
import simulation.World
import simulation.interpolateWorlds

val getMarchedBuffers = singleCache(::newMarchBuffers)

fun renderRayMarching(gameScene: GameScene, dimensions: Vector2i, world: World, renderer: Renderer, marcher: Marcher) {
  val buffers = getMarchedBuffers(dimensions.x * dimensions.y)
  val scene = Scene(
      camera = Camera(
          position = gameScene.camera.position,
          orientation = gameScene.camera.orientation,
          near = 0.01f,
          far = 1000f
      ),
      sdf = prepareSceneSdf(world.players.first().id, world),
      lights = listOf()
  )
  val cast = perspectiveRay(scene.camera)

  renderToMarchBuffers(buffers, marcher, scene, cast, dimensions)
  val mix = postPipeline(dimensions, buffers)
  rgbFloatToBytes(mix, renderer.renderColor.buffer!!)
  normalizeDepthBuffer(0.01f, 1000f, buffers.depth, renderer.renderDepth.buffer!!)
  renderer.applyRenderBuffer(dimensions)
}

fun renderMain(client: Client, windowInfo: WindowInfo, appState: AppState, boxes: Boxes) {
  client.platform.display.swapBuffers()

  val marcher = Marcher(
      end = 100f,
      maxSteps = 100
  )

  renderContainer(client, windowInfo) { canvas ->
    val world = interpolateWorlds(appState.timestep.accumulator, appState.worlds)
    if (world != null) {
      val scenes = createScenes(world, client.screens)
      val dimensions = windowInfo.dimensions
      val viewports = getPlayerViewports(scenes.size, dimensions).iterator()
      val scene = scenes.first()
//      for (s in scenes) {
      val sceneRenderer = client.renderer.createSceneRenderer(scene.main, viewports.next())
      val gameRenderer = GameSceneRenderer(scene, sceneRenderer)
      val filters = prepareRender(gameRenderer)
      renderScene(gameRenderer)

      if (false) {
        renderRayMarching(scene, dimensions, world, client.renderer, marcher)
      }

      finishRender(gameRenderer, filters)
//      }
    }

//      val bounds = Bounds(Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y))
    renderLayout(boxes, canvas)
//      renderGui(client, bounds, canvas, world, appState.client)
  }
}

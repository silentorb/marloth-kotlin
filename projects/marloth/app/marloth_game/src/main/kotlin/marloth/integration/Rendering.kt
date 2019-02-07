package marloth.integration

import marloth.clienting.Client
import marloth.clienting.renderContainer
import marloth.clienting.renderScene
import mythic.bloom.Boxes
import mythic.bloom.renderLayout
import mythic.ent.singleCache
import mythic.platforming.WindowInfo
import rendering.GameSceneRenderer
import rendering.getPlayerViewports
import silentorb.raymarching.*
import simulation.interpolateWorlds

val getMarchedBuffers = singleCache(::newMarchBuffers)

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
      val buffers = getMarchedBuffers(dimensions.x * dimensions.y)

      for (s in scenes) {
        val sceneRenderer = client.renderer.createSceneRenderer(s.main, viewports.next())
        val gameRenderer = GameSceneRenderer(s, sceneRenderer)
        val scene = Scene(
            camera = Camera(
                position = s.camera.position,
                orientation = s.camera.orientation,
                near = 0.01f,
                far = 1000f
            ),
            sdf = prepareSceneSdf(world.players.first().id, world),
            lights = listOf()
        )
        val cast = perspectiveRay(scene.camera)

        renderToMarchBuffers(buffers, marcher, scene, cast, dimensions)
        val buffer = client.renderer.renderTextureBuffer!!
        postPipeline(dimensions, buffers, buffer)
        client.renderer.applyRenderBuffer(windowInfo)
        renderScene(gameRenderer)
      }
    }

//      val bounds = Bounds(Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y))
    renderLayout(boxes, canvas)
//      renderGui(client, bounds, canvas, world, appState.client)
  }
}

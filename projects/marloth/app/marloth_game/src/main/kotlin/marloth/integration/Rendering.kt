package marloth.integration

import marloth.clienting.Client
import marloth.clienting.renderContainer
import marloth.clienting.renderScene
import mythic.bloom.Boxes
import mythic.bloom.renderLayout
import mythic.platforming.WindowInfo
import rendering.GameSceneRenderer
import rendering.getPlayerViewports
import simulation.interpolateWorlds

fun renderMain(client: Client, windowInfo: WindowInfo, appState: AppState, boxes: Boxes) {
  client.platform.display.swapBuffers()

  renderContainer(client, windowInfo) { canvas ->
    val world = interpolateWorlds(appState.timestep.accumulator, appState.worlds)
    if (world != null) {
      val scenes = createScenes(world, client.screens)
      val viewports = getPlayerViewports(scenes.size, windowInfo.dimensions).iterator()
      for (scene in scenes) {
        val sceneRenderer = client.renderer.createSceneRenderer(scene.main, viewports.next())
        val gameRenderer = GameSceneRenderer(scene, sceneRenderer)
        renderScene(gameRenderer)
      }
    }

//      val bounds = Bounds(Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y))
    renderLayout(boxes, canvas)
//      renderGui(client, bounds, canvas, world, appState.client)
  }
}

package front

import marloth.clienting.Client
import marloth.clienting.gui.renderGui
import marloth.clienting.renderContainer
import marloth.clienting.renderScene
import mythic.bloom.Bounds
import org.joml.Vector4i
import rendering.GameSceneRenderer
import rendering.getPlayerViewports
import scenery.Screen
import visualizing.createScenes

fun renderMain(client: Client, appState: AppState) {
  renderContainer(client) { canvas, windowInfo ->
    val world = appState.world
    if (world != null) {
      val scenes = createScenes(world, client.screens)
      val viewports = getPlayerViewports(scenes.size, windowInfo.dimensions).iterator()
      for (scene in scenes) {
        val sceneRenderer = client.renderer.createSceneRenderer(scene.main, viewports.next())
        val gameRenderer = GameSceneRenderer(scene, sceneRenderer)
        renderScene(gameRenderer)
      }
    }

    renderGui(Bounds(Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)), canvas, world, appState.client.menu)
  }
}
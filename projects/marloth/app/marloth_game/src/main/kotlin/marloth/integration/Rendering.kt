package marloth.integration

import marloth.clienting.*
import marloth.front.RenderHook
import marloth.scenery.creation.createScene
import mythic.bloom.next.Box
import mythic.bloom.renderLayout
import mythic.platforming.WindowInfo
import org.joml.Vector2i
import org.joml.Vector4i
import rendering.GameSceneRenderer
import rendering.createCanvas
import rendering.createSceneRenderer
import simulation.misc.interpolateWorlds

fun renderMain(client: Client, windowInfo: WindowInfo, appState: AppState, boxes: List<Box>, viewports: List<Vector4i>,
               onRender: RenderHook?) {
  client.platform.display.swapBuffers()

  renderContainer(client, windowInfo) {
    val world = interpolateWorlds(appState.timestep.accumulator, appState.worlds)
    if (world != null) {
      val scenes = appState.client.players.mapNotNull(createScene(world.deck))
      val viewportIterator = viewports.iterator()
      scenes.zip(boxes) { scene, box ->
        val screenViewport = viewportIterator.next()
        val sceneRenderer = createSceneRenderer(client.renderer, scene, screenViewport)
        val gameRenderer = GameSceneRenderer(scene, sceneRenderer)
        val filters = prepareRender(gameRenderer)

        renderScene(gameRenderer)

        if (onRender != null) {
          onRender(sceneRenderer)
        }
        val canvas = createCanvas(client.renderer, Vector2i(screenViewport.z, screenViewport.w))
        renderLayout(box, canvas)
        finishRender(gameRenderer, filters)
      }
    }
  }
}

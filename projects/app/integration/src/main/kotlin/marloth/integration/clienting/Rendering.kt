package marloth.integration.clienting

import marloth.clienting.Client
import marloth.clienting.rendering.createSceneRenderer
import marloth.clienting.rendering.prepareRender
import marloth.integration.debug.labRender
import marloth.integration.misc.AppState
import marloth.integration.scenery.createScene
import silentorb.mythic.bloom.next.Box
import silentorb.mythic.bloom.renderLayout
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.lookinglass.*
import silentorb.mythic.lookinglass.texturing.updateAsyncTextureLoading
import silentorb.mythic.platforming.WindowInfo
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4i
import simulation.misc.interpolateWorlds

fun renderMain(client: Client, windowInfo: WindowInfo, appState: AppState, boxes: List<Box>, viewports: List<Vector4i>) {
  updateAsyncTextureLoading(client.textureLoader, client.renderer.textures)
//  updateOffscreenBufferAllocations(client.renderer, )
  val renderer = client.renderer
  prepareRender(renderer, windowInfo)
  val world =
      if (getDebugBoolean("DISABLE_INTERPOLATION"))
        appState.worlds.lastOrNull()
      else
        interpolateWorlds(appState.timestep.accumulator, appState.worlds)
  if (world != null) {
    val scenes = appState.client.players
        .map(createScene(world.definitions, world.deck, appState.client.targetings))

    val viewportIterator = viewports.iterator()
    scenes.zip(boxes) { scene, box ->
      val screenViewport = viewportIterator.next()
      val renderer = client.renderer
      val sceneRenderer = createSceneRenderer(client.renderer, scene, screenViewport)
      val filters = prepareRender(sceneRenderer, scene)
      renderSceneLayers(renderer, sceneRenderer.camera, scene.layers)
      labRender(appState)(sceneRenderer, scene.main)
      val dimensions = Vector2i(screenViewport.z, screenViewport.w)
      val canvas = createCanvas(client.renderer, client.customBloomResources, dimensions)
      applyFilters(sceneRenderer, filters)
      renderLayout(box, canvas, getDebugBoolean("MARK_BLOOM_PASS"))
    }
  }
  finishRender(renderer, windowInfo)
  client.platform.display.swapBuffers()
}

package marloth.integration.clienting

import marloth.clienting.Client
import marloth.integration.debug.labRender
import marloth.integration.misc.AppState
import marloth.integration.scenery.createScene
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.renderLayout
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.*
import silentorb.mythic.lookinglass.texturing.updateAsyncTextureLoading
import silentorb.mythic.platforming.WindowInfo
import silentorb.mythic.spatial.Vector4i
import simulation.misc.interpolateWorlds
import simulation.updating.getIdle

fun renderMain(client: Client, windowInfo: WindowInfo, appState: AppState, boxes: Map<Id, Box>, viewports: List<Vector4i>
) {
  val renderer = client.renderer

  updateAsyncTextureLoading(client.textureLoadingState, renderer.textures)
  prepareRender(renderer, windowInfo)
  val world =
      if (getDebugBoolean("DISABLE_INTERPOLATION"))
        appState.worlds.lastOrNull()
      else
        interpolateWorlds(appState.timestep.accumulator, appState.worlds)

  if (world != null) {
    val scenes = appState.client.players
        .map(createScene(renderer.meshes, world))

    val viewportIterator = viewports.iterator()
    val boxIterator = boxes.values.iterator()

    for (scene in scenes) {
      val screenViewport = viewportIterator.next()
      val dimensions = screenViewport.zw()
      val canvas = createCanvas(client.renderer, client.customBloomResources, dimensions)
      val sceneRenderer = createSceneRenderer(client.renderer, windowInfo, scene, screenViewport)
      val filters = prepareRender(sceneRenderer, scene)
      val idleTime = getIdle(appState.timestep.increment)
      if (getDebugBoolean("RENDER_MARCHING")) {
//        currentMarching = updateMarchingMain(sceneRenderer, client.impModels, idleTime, scene.layers, currentMarching)
        renderSceneLayers(sceneRenderer,sceneRenderer.camera, scene.layers)
      } else {
        profileGpu("main") {
          renderSceneLayers(sceneRenderer, sceneRenderer.camera, scene.layers)
        }
      }
      labRender(appState)(sceneRenderer, scene.main)
      applyFilters(sceneRenderer, filters)
      if (boxIterator.hasNext()) {
        val box = boxIterator.next()
        renderLayout(box, canvas, getDebugBoolean("MARK_BLOOM_PASS"))
      }
    }
  }

  val onRenderPost = appState.hooks?.onRenderPost
  if (onRenderPost != null) {
    onRenderPost(windowInfo, appState)
  }

  finishRender(renderer, windowInfo)
  client.platform.display.swapBuffers()
}

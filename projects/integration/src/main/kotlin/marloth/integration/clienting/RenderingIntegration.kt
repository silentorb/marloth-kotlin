package marloth.integration.clienting

import marloth.clienting.Client
import marloth.clienting.rendering.createSceneRenderer
import marloth.clienting.rendering.marching.MarchingState
import marloth.clienting.rendering.marching.updateMarchingMain
import marloth.clienting.rendering.prepareRender
import marloth.clienting.rendering.renderLayersWithMarching
import marloth.integration.debug.labRender
import marloth.integration.editing.sceneFromEditorGraph
import marloth.integration.misc.AppState
import marloth.integration.scenery.createScene
import marloth.integration.scenery.defaultLightingConfig
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.renderLayout
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.editing.panels.defaultViewportId
import silentorb.mythic.lookinglass.*
import silentorb.mythic.lookinglass.texturing.updateAsyncTextureLoading
import silentorb.mythic.platforming.WindowInfo
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4i
import simulation.misc.interpolateWorlds
import simulation.updating.getIdle

fun renderMain(client: Client, windowInfo: WindowInfo, appState: AppState, boxes: Collection<Box>, viewports: List<Vector4i>
): MarchingState {
  val renderer = client.renderer

  updateAsyncTextureLoading(client.textureLoadingState, renderer.textures)
  prepareRender(renderer, windowInfo)
  var currentMarching = appState.client.marching
  val world =
      if (getDebugBoolean("DISABLE_INTERPOLATION"))
        appState.worlds.lastOrNull()
      else
        interpolateWorlds(appState.timestep.accumulator, appState.worlds)

  if (world != null) {
    val isEditing = appState.client.isEditorActive ?: false
    val scenes = if (isEditing)
      listOf(sceneFromEditorGraph(renderer.meshes, appState.client.editor!!, defaultLightingConfig(), defaultViewportId))
    else
      appState.client.players
          .map(createScene(renderer.meshes, client.impModels, world.definitions, world.deck))

    val viewportIterator = viewports.iterator()

    scenes.zip(boxes) { scene, box ->
      val screenViewport = viewportIterator.next()
      val dimensions = Vector2i(screenViewport.z, screenViewport.w)
      val canvas = createCanvas(client.renderer, client.customBloomResources, dimensions)
      val sceneRenderer = createSceneRenderer(client.renderer, scene, screenViewport)
      val filters = prepareRender(sceneRenderer, scene)
      val idleTime = getIdle(appState.timestep.increment)
      if (getDebugBoolean("RENDER_MARCHING")) {
        currentMarching = updateMarchingMain(sceneRenderer, client.impModels, idleTime, scene.layers, currentMarching)
        renderLayersWithMarching(sceneRenderer, scene.layers, currentMarching.gpu)
      } else {
        renderSceneLayers(sceneRenderer, sceneRenderer.camera, scene.layers)
      }
      labRender(appState)(sceneRenderer, scene.main)
      applyFilters(sceneRenderer, filters)
      renderLayout(box, canvas, getDebugBoolean("MARK_BLOOM_PASS"))
    }
  }

  val onRenderPost = appState.hooks?.onRenderPost
  if (onRenderPost != null) {
    onRenderPost(windowInfo, appState)
  }

  finishRender(renderer, windowInfo)
  client.platform.display.swapBuffers()
  return currentMarching
}

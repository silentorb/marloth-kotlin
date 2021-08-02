package marloth.integration.clienting

import marloth.clienting.Client
import marloth.clienting.gui.hud.overlayLayout
import marloth.clienting.rendering.renderLoadingScreen
import marloth.integration.debug.labRender
import marloth.integration.misc.AppState
import marloth.integration.scenery.createScene
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.Seed
import silentorb.mythic.bloom.renderLayout
import silentorb.mythic.bloom.toAbsoluteBoundsRecursive
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.drawing.Canvas
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.*
import silentorb.mythic.lookinglass.pipeline.applyFilters
import silentorb.mythic.lookinglass.pipeline.applyRenderedBuffers
import silentorb.mythic.lookinglass.pipeline.finishRender
import silentorb.mythic.lookinglass.pipeline.prepareRender
import silentorb.mythic.lookinglass.updateAsyncTextureLoading
import silentorb.mythic.platforming.WindowInfo
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4i
import simulation.main.World
import simulation.misc.interpolateWorlds
import simulation.updating.getIdle

fun renderOverlay(appState: AppState, world: World, canvas: Canvas, dimensions: Vector2i, player: Id) {
  val clientState = appState.client
  val guiState = clientState.guiStates[player]
  val hudDebugInfo = getHudDebugInfo(appState)
  val overlay = overlayLayout(world.definitions.textLibrary, world, appState.options, clientState, player, hudDebugInfo)
  if (overlay != null) {
    val seed = Seed(
        dimensions = dimensions,
        state = guiState?.bloomState ?: mapOf()
    )
    val nestedBox = overlay(seed)
    val box = toAbsoluteBoundsRecursive(nestedBox)
    renderLayout(box, canvas, getDebugBoolean("MARK_BLOOM_GUI_PASS"))
  }
}

fun renderMain(client: Client, windowInfo: WindowInfo, appState: AppState, boxes: Map<Id, Box>, viewports: List<Vector4i>
) {
  val renderer = client.renderer
  profileGpu("all") {
    updateAsyncTextureLoading(client.textureLoadingState, renderer.textures)
    prepareRender(renderer, windowInfo)
    if (appState.client.activeLoadingTasks.any()) {
      renderLoadingScreen(client, windowInfo)
    } else {
      val world =
          if (getDebugBoolean("DISABLE_INTERPOLATION"))
            appState.worlds.lastOrNull()
          else
            interpolateWorlds(appState.timestep.accumulator, appState.worlds)

      if (world != null) {
        val scenes = appState.client.players
            .associateWith(createScene(client.resourceInfo, world))

        val viewportIterator = viewports.iterator()
        val boxIterator = boxes.values.iterator()

        for ((player, scene) in scenes) {
          val screenViewport = viewportIterator.next()
          val dimensions = screenViewport.zw()
          val canvas = createCanvas(client.renderer, client.customBloomResources, dimensions)
          val sceneRenderer = createSceneRenderer(client.renderer, windowInfo, scene, screenViewport)
          val filters = prepareRender(sceneRenderer, scene)
          val idleTime = getIdle(appState.timestep.increment)
          if (getDebugBoolean("RENDER_MARCHING")) {
//        currentMarching = updateMarchingMain(sceneRenderer, client.impModels, idleTime, scene.layers, currentMarching)
            renderSceneLayers(sceneRenderer, sceneRenderer.camera, scene.layers)
          } else {
            renderSceneLayers(sceneRenderer, sceneRenderer.camera, scene.layers)
          }
          labRender(appState)(sceneRenderer, scene)
          renderSceneLayerHighlights(sceneRenderer, scene.layers)
          renderOverlay(appState, world, canvas, dimensions, player)
          applyFilters(sceneRenderer, filters)
          if (boxIterator.hasNext()) {
            val box = boxIterator.next()
            renderLayout(box, canvas, getDebugBoolean("MARK_BLOOM_GUI_PASS"))
          }
        }
      } else {
        val viewport = viewports.firstOrNull()
        val box = boxes.values.firstOrNull()
        if (viewport != null && box != null) {
          val dimensions = viewport.zw()
          val canvas = createCanvas(client.renderer, client.customBloomResources, dimensions)
          applyRenderedBuffers(renderer, windowInfo)
          renderLayout(box, canvas, getDebugBoolean("MARK_BLOOM_GUI_PASS"))
        }
      }
    }

    val onRenderPost = appState.hooks?.onRenderPost
    if (onRenderPost != null) {
      onRenderPost(windowInfo, appState)
    }

    finishRender(renderer, windowInfo)
  }
  client.platform.display.swapBuffers()
}

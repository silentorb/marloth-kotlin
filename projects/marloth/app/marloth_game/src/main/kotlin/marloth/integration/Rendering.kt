package marloth.integration

import marloth.clienting.Client
import marloth.clienting.rendering.createSceneRenderer
import marloth.clienting.rendering.prepareRender
import marloth.debug.labRender
import marloth.front.RenderHook
import marloth.scenery.creation.createScene
import silentorb.mythic.bloom.next.Box
import silentorb.mythic.bloom.renderLayout
import silentorb.mythic.lookinglass.*
import silentorb.mythic.lookinglass.drawing.renderBackground
import silentorb.mythic.lookinglass.texturing.updateAsyncTextureLoading
import silentorb.mythic.platforming.WindowInfo
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4i
import simulation.misc.interpolateWorlds

fun renderMain(client: Client, windowInfo: WindowInfo, appState: AppState, boxes: List<Box>, viewports: List<Vector4i>,
               onRender: RenderHook?) {
  client.platform.display.swapBuffers()

  updateAsyncTextureLoading(client.textureLoader, client.renderer.textures)

  renderContainer(client.renderer, windowInfo) {
    val world = interpolateWorlds(appState.timestep.accumulator, appState.worlds)
    if (world != null) {
      val scenes = appState.client.players.map(createScene(world.definitions, world.deck))
      val viewportIterator = viewports.iterator()
      scenes.zip(boxes) { scene, box ->
        val screenViewport = viewportIterator.next()
        val renderer = client.renderer
        val sceneRenderer = createSceneRenderer(client.renderer, scene, screenViewport)
        val filters = prepareRender(sceneRenderer, scene)

//        renderBackground(renderer, sceneRenderer.camera, scene.background)
//        renderElements(sceneRenderer.renderer, sceneRenderer.camera, scene.opaqueElementGroups, scene.transparentElementGroups)
        renderSceneLayers(renderer, sceneRenderer.camera, scene.layers)

//        if (onRender != null) {
          labRender(appState)(sceneRenderer, scene.main)
//          onRender(sceneRenderer, scene.main)
//        }
        val canvas = createCanvas(client.renderer, Vector2i(screenViewport.z, screenViewport.w))
        renderLayout(box, canvas)
        finishRender(sceneRenderer, filters)
      }
    }
  }
}

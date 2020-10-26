package marloth.clienting.rendering

import marloth.clienting.rendering.marching.*
import silentorb.mythic.glowing.globalState
import silentorb.mythic.lookinglass.*
import silentorb.mythic.scenery.Light
import silentorb.mythic.spatial.Vector4i

fun prepareRender(renderer: SceneRenderer, scene: GameScene): List<ScreenFilter> {
  val filters = getDisplayConfigFilters(renderer.renderer.options).plus(scene.filters)
  prepareRender(renderer, filters)
  globalState.lineThickness = 2f
  return filters
}

fun gatherSceneLights(meshes: ModelMeshMap, scene: GameScene): List<Light> {
  return scene.main.lights
//      .plus(gatherChildLights(meshes, scene.opaqueElementGroups))
}

fun createSceneRenderer(renderer: Renderer, scene: GameScene, viewport: Vector4i): SceneRenderer {
  val minimalScene = scene.main.copy(
      lights = gatherSceneLights(renderer.meshes, scene)
  )
  return createSceneRenderer(renderer, minimalScene, viewport)
}

fun renderLayersWithMarching(sceneRenderer: SceneRenderer, layers: SceneLayers, marchingGpu: MarchingGpuState) {
  renderSceneLayers(sceneRenderer, sceneRenderer.camera, layers) { _, _, layer ->
    if (layer.attributes.contains(marchingRenderLayer)) {
      drawMarching(sceneRenderer.renderer, marchingGpu)
    }
  }
}

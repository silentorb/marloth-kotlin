package marloth.clienting.rendering

import marloth.clienting.rendering.marching.*
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.glowing.globalState
import silentorb.mythic.lookinglass.*
import silentorb.mythic.scenery.Light
import silentorb.mythic.spatial.Vector4i

fun prepareRender(renderer: SceneRenderer, scene: GameScene): List<ScreenFilter> {
  val filters = getDisplayConfigFilters(renderer.renderer.config).plus(scene.filters)
  prepareRender(renderer, filters)
  globalState.lineThickness = 2f
  return filters
}

fun gatherSceneLights(meshes: ModelMeshMap, scene: GameScene): List<Light> {
  return scene.lights
//      .plus(gatherChildLights(meshes, scene.opaqueElementGroups))
}

fun createSceneRenderer(renderer: Renderer, scene: GameScene, viewport: Vector4i): SceneRenderer {
  val minimalScene = scene.main.copy(
      lights = gatherSceneLights(renderer.meshes, scene)
  )
  return createSceneRenderer(renderer, minimalScene, viewport)
}

fun updateMarchingMain(sceneRenderer: SceneRenderer, impModels: Map<String, ModelFunction>, layers: SceneLayers, marchingGpu: MarchingGpuState): MarchingGpuState {
  val allElements = layers.flatMap { it.elements }
  val vertexSchema = sceneRenderer.renderer.vertexSchemas.shadedColor
  val sources = updateMarching(impModels, sceneRenderer.camera, allElements, marchingGpu.meshes.keys)
  renderMarchingLab(sceneRenderer, impModels, sceneRenderer.camera, allElements)
  return updateMarchingGpu(vertexSchema, sources, marchingGpu)
}

const val marchingRenderLayer: String = "marchingLayer"

fun renderLayersWithMarching(sceneRenderer: SceneRenderer, layers: SceneLayers, marchingGpu: MarchingGpuState) {
  renderSceneLayers(sceneRenderer, sceneRenderer.camera, layers) { _, _, layer ->
    if (layer.attributes.contains(marchingRenderLayer)) {
      drawMarching(sceneRenderer.renderer, marchingGpu)
    }
  }
}

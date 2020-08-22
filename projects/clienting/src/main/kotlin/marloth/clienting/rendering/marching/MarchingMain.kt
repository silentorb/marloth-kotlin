package marloth.clienting.rendering.marching

import marloth.clienting.rendering.marching.services.gatherNeededCells
import marloth.clienting.rendering.marching.services.marchingCoordinates
import marloth.clienting.rendering.marching.services.renderNewCells
import silentorb.mythic.fathom.mergeDistanceFunctionsTrackingIds
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.fathom.misc.mergeShadingFunctions
import silentorb.mythic.glowing.*
import silentorb.mythic.lookinglass.ElementGroups
import silentorb.mythic.lookinglass.Renderer
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.lookinglass.shading.ObjectShaderConfig
import silentorb.mythic.lookinglass.shading.ShaderFeatureConfig
import silentorb.mythic.scenery.Camera
import silentorb.mythic.spatial.Vector3i

fun updateMarching(models: Map<String, ModelFunction>, camera: Camera, allElements: ElementGroups, previousCells: Set<Vector3i>):CellSourceMeshes {
  val elements = filterModels(models, allElements)
  return if (elements.any()) {
    val forms = mapElementTransforms(models, elements)
    val form = mergeDistanceFunctionsTrackingIds(forms)
    val points = gatherNeededCells(camera, form, marchingCoordinates())
    val cells = points
        .map(::toCellVector3i)
    val newCells = cells - previousCells
    val shading = mergeShadingFunctions(forms, form)
    renderNewCells(form, shading, newCells)
  } else
    mapOf()
}

fun updateMarchingGpu(vertexSchema: VertexSchema, sources: CellSourceMeshes, state: MarchingGpuState): MarchingGpuState {
  val newGpuMeshes = cellMeshesToGpuMeshes(vertexSchema, sources)
  return state.copy(
      meshes = state.meshes + newGpuMeshes
  )
}

fun drawMarchingMeshes(renderer: Renderer, meshes: Collection<GeneralMesh>) {
  if (meshes.none())
    return

  val vertexSchema = renderer.vertexSchemas.shadedColor

  val effect = renderer.getShader(vertexSchema, ShaderFeatureConfig(
      colored = true,
      shading = true
  ))
  effect.activate(ObjectShaderConfig())
  globalState.depthEnabled = true
  for (mesh in meshes) {
    drawMesh(mesh, DrawMethod.triangleFan)
  }
}

fun drawMarching(renderer: Renderer, state: MarchingGpuState) {
  drawMarchingMeshes(renderer, state.meshes.values)
}

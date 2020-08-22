package marloth.clienting.rendering.marching

import marloth.clienting.rendering.marching.services.gatherNeededCells
import marloth.clienting.rendering.marching.services.renderNewCells
import silentorb.mythic.fathom.mergeDistanceFunctionsTrackingIds
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.fathom.misc.mergeShadingFunctions
import silentorb.mythic.glowing.DrawMethod
import silentorb.mythic.glowing.GeneralMesh
import silentorb.mythic.glowing.VertexSchema
import silentorb.mythic.glowing.drawMesh
import silentorb.mythic.lookinglass.Renderer
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.lookinglass.SceneRenderer
import silentorb.mythic.lookinglass.shading.ObjectShaderConfig
import silentorb.mythic.lookinglass.shading.ShaderFeatureConfig
import silentorb.mythic.scenery.Camera
import silentorb.mythic.spatial.Vector3i

fun updateMarching(models: Map<String, ModelFunction>, camera: Camera, layer: SceneLayer, previousCells: Set<Vector3i>):CellSourceMeshes {
  val elements = filterModels(models, layer.elements)
  return if (elements.any()) {
    val forms = mapElementTransforms(models, elements)
    val form = mergeDistanceFunctionsTrackingIds(forms)
    val points = gatherNeededCells(camera, form)
    val cells = points
        .map { (x, y, z) ->
          Vector3i(
              x.toInt(),
              y.toInt(),
              z.toInt()
          )
        }
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

  val effect = renderer.getShader(vertexSchema, ShaderFeatureConfig())
  effect.activate(ObjectShaderConfig())
  for (mesh in meshes) {
    drawMesh(mesh, DrawMethod.triangleFan)
  }
}

fun drawMarching(renderer: Renderer, state: MarchingGpuState) {
  drawMarchingMeshes(renderer, state.meshes.values)
}

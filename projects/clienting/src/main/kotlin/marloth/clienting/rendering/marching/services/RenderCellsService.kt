package marloth.clienting.rendering.marching.services

import marloth.clienting.rendering.marching.MarchingModelMesh
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.fathom.marching.marchingMesh
import silentorb.mythic.fathom.mergeDistanceFunctionsTrackingIds
import silentorb.mythic.fathom.misc.DistanceFunction
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.fathom.misc.ShadingFunction
import silentorb.mythic.fathom.misc.mergeShadingFunctions
import silentorb.mythic.fathom.surfacing.GridBounds
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.Shading
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.spatial.toVector3

fun renderModel(form: DistanceFunction, shading: ShadingFunction, bounds: GridBounds): MarchingModelMesh {
  val voxelsPerUnit = getDebugInt("MESH_RESOLUTION") ?: 10 // (if (Dice().getBoolean()) 5 else 15 )
  val (vertices, triangles) = marchingMesh(voxelsPerUnit, form, shading, bounds)
  return MarchingModelMesh(
      vertices = vertices,
      triangles = triangles
  )
}

fun renderNewCells(models: Collection<ModelFunction>, cells: Collection<Vector3i>): Map<Vector3i, MarchingModelMesh> {
  return cells.associateWith { cell ->
    val bounds = GridBounds(cell, cell + 1)
    val center = cell.toVector3() + 0.5f
    val localModels = models.filter { model ->
      val (_, distance) = model.form(center)
      distance < 1.5f
    }

    val form = mergeDistanceFunctionsTrackingIds(localModels)
    val shading = mergeShadingFunctions(localModels, form)
    renderModel(form, shading, bounds)
  }
}

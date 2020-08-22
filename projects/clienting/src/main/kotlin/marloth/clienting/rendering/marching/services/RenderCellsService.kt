package marloth.clienting.rendering.marching.services

import marloth.clienting.rendering.marching.MarchingModelMesh
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.fathom.marching.marchingMesh
import silentorb.mythic.fathom.misc.DistanceFunction
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.fathom.misc.ShadingFunction
import silentorb.mythic.fathom.surfacing.GridBounds
import silentorb.mythic.scenery.Shading
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i

fun renderModel(form: DistanceFunction, shading: ShadingFunction, bounds: GridBounds): MarchingModelMesh {
  val voxelsPerUnit = getDebugInt("MESH_RESOLUTION") ?: 10
  val (vertices, triangles) = marchingMesh(voxelsPerUnit, form, shading, bounds)
  return MarchingModelMesh(
      vertices = vertices,
      triangles = triangles
  )
}

fun renderNewCells(form: DistanceFunction, shading: ShadingFunction, cells: Collection<Vector3i>): Map<Vector3i, MarchingModelMesh> {
  return cells.associateWith { cell ->
    val bounds = GridBounds(cell, cell + 1)
    renderModel(form, shading, bounds)
  }
}

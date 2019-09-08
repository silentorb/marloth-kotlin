package simulation.intellect.navigation

import org.recast4j.detour.NavMeshDataCreateParams
import org.recast4j.recast.RecastBuilder
import org.recast4j.recast.geom.InputGeomProvider

fun newNavMeshDataCreateParams(geometry: InputGeomProvider, builderResult: RecastBuilder.RecastBuilderResult): NavMeshDataCreateParams {
  val mesh = builderResult.mesh
  val params = NavMeshDataCreateParams()
  params.verts = mesh.verts
  params.vertCount = mesh.nverts
  params.polys = mesh.polys
  params.polyAreas = mesh.areas
  params.polyFlags = mesh.flags
  params.polyCount = mesh.npolys
  params.nvp = mesh.nvp
  val detailedMesh = builderResult.getMeshDetail()
  if (detailedMesh != null) {
    params.detailMeshes = detailedMesh.meshes
    params.detailVerts = detailedMesh.verts
    params.detailVertsCount = detailedMesh.nverts
    params.detailTris = detailedMesh.tris
    params.detailTriCount = detailedMesh.ntris
  }
  params.cs = cellSize
  params.ch = cellHeight
  params.walkableHeight = agentHeight
  params.walkableRadius = agentRadius
  params.walkableClimb = agentMaxClimb
  params.bmin = mesh.bmin
  params.bmax = mesh.bmax
  params.buildBvTree = true
  return params
}

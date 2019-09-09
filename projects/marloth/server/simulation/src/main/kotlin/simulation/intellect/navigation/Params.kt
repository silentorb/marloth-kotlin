package simulation.intellect.navigation

import org.recast4j.detour.NavMeshDataCreateParams
import org.recast4j.recast.RecastBuilder
import org.recast4j.recast.RecastConfig
import org.recast4j.recast.RecastConstants
import org.recast4j.recast.geom.InputGeomProvider

const val cellSize = 0.3f
const val cellHeight = 0.2f
const val agentHeight = 2f
const val agentRadius = 0.3f
const val agentMaxClimb = 0.9f

val vertsPerPoly = 6

fun newRecastConfig() =
    RecastConfig(
        RecastConstants.PartitionType.WATERSHED,
        cellSize,
        cellHeight,
        agentHeight,
        agentRadius,
        agentMaxClimb,
        45f,
        3,
        20,
        12f,
        1.3f,
        vertsPerPoly,
        6f,
        1f,
        0,
        walkable,
        true,
        true,
        true
    )

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

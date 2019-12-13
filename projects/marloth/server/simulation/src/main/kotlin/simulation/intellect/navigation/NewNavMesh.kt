package simulation.intellect.navigation

import silentorb.mythic.ent.firstFloatSortedBy
import silentorb.mythic.ent.firstFloatSortedByDescending
import org.recast4j.detour.NavMesh
import org.recast4j.detour.NavMeshBuilder
import org.recast4j.recast.Heightfield
import org.recast4j.recast.RecastBuilder
import org.recast4j.recast.RecastBuilderConfig
import org.recast4j.recast.geom.TriMesh
import simulation.main.Deck
import simulation.misc.MapGrid
import simulation.misc.cellLength

var originalNavMeshData: List<TriMesh> = listOf()
var globalHeightMap: Heightfield? = null

fun newNavMesh(grid: MapGrid, deck: Deck): NavMesh {
  val elements = deck.architecture
  val meshes = newNavMeshTriMeshes(deck)
  val vertices = meshes.flatMap { it.verts.toList() }
  originalNavMeshData = meshes

  val padding = cellLength * 2f
//  val minBounds = floatArrayOf(-100f, -100f, -100f)
//  val maxBounds = floatArrayOf(100f, 100f, 100f)

//  val minBounds = floatArrayOf(
//      grid.cells.keys.map { it.x }.firstIntSortedBy { it }.toFloat() * cellLength - padding,
//      grid.cells.keys.map { it.y }.firstIntSortedBy { it }.toFloat() * cellLength - padding,
//      grid.cells.keys.map { it.z }.firstIntSortedBy { it }.toFloat() * cellLength - padding
//  )
//
//  val maxBounds = floatArrayOf(
//      grid.cells.keys.map { it.x }.firstIntSortedByDescending { it }.toFloat() * cellLength + padding,
//      grid.cells.keys.map { it.y }.firstIntSortedByDescending { it }.toFloat() * cellLength + padding,
//      grid.cells.keys.map { it.z }.firstIntSortedByDescending { it }.toFloat() * cellLength + padding
//  )
  val minBounds = floatArrayOf(
//      elements
//          .map { deck.bodies[it.key]!!.position.x - deck.collisionShapes[it.key]!!.shape.radius }
//          .firstSortedBy { it } - padding,
//      elements
//          .map { deck.bodies[it.key]!!.position.z - deck.collisionShapes[it.key]!!.shape.radius }
//          .firstSortedBy { it } - padding,
//      elements
//          .map { deck.bodies[it.key]!!.position.y - deck.collisionShapes[it.key]!!.shape.radius }
//          .firstSortedBy { it } - padding
      (0 until vertices.size step 3).map { vertices[it] }.firstFloatSortedBy { it } - padding,
      (1 until vertices.size step 3).map { vertices[it] }.firstFloatSortedBy { it } - padding,
      (2 until vertices.size step 3).map { vertices[it] }.firstFloatSortedBy { it } - padding
  )

  val maxBounds = floatArrayOf(
//      elements
//          .map { deck.bodies[it.key]!!.position.x + deck.collisionShapes[it.key]!!.shape.radius }
//          .firstSortedByDescending { it } + padding,
//      elements
//          .map { deck.bodies[it.key]!!.position.z + deck.collisionShapes[it.key]!!.shape.radius }
//          .firstSortedByDescending { it } + padding,
//      elements
//          .map { deck.bodies[it.key]!!.position.y + deck.collisionShapes[it.key]!!.shape.radius }
//          .firstSortedByDescending { it } + padding
      (0 until vertices.size step 3).map { vertices[it] }.firstFloatSortedByDescending { it } + padding,
      (1 until vertices.size step 3).map { vertices[it] }.firstFloatSortedByDescending { it } + padding,
      (2 until vertices.size step 3).map { vertices[it] }.firstFloatSortedByDescending { it } + padding
  )
  val geometry = GeometryProvider(
      meshes.toMutableList(),
      mutableListOf(),
      minBounds,
      maxBounds
  )

  val recastConfig = newRecastConfig()
  val builderConfig = RecastBuilderConfig(recastConfig, minBounds, maxBounds)
  val builder = RecastBuilder()
  val buildResult = builder.build(geometry, builderConfig)
  val params = newNavMeshDataCreateParams(geometry, buildResult)
  val meshData = NavMeshBuilder.createNavMeshData(params)
  if (meshData == null)
    throw Error("Error generating NavMesh")

  globalHeightMap = buildResult.solidHeightfield
  return NavMesh(meshData, vertsPerPoly, 0)
}

package simulation.intellect.navigation

import mythic.ent.Id
import mythic.ent.firstSortedBy
import mythic.ent.firstSortedByDescending
import mythic.spatial.Vector3
import org.recast4j.detour.NavMesh
import org.recast4j.detour.NavMeshBuilder
import org.recast4j.detour.NavMeshDataCreateParams
import org.recast4j.recast.*
import org.recast4j.recast.geom.ChunkyTriMesh
import org.recast4j.recast.geom.InputGeomProvider
import org.recast4j.recast.geom.TriMesh
import scenery.Box
import scenery.Cylinder
import scenery.Shape
import simulation.main.Deck
import simulation.physics.getBodyTransform

var originalNavMeshData: List<TriMesh> = listOf()
var globalHeightMap: Heightfield? = null

fun newNavMesh(deck: Deck): NavMesh {
  val elements = deck.architecture
  val meshes = newNavMeshTriMeshes(deck)
  val vertices = meshes.flatMap { it.verts.toList() }
  originalNavMeshData = meshes

  val padding = 0f
//  val minBounds = floatArrayOf(-100f, -100f, -100f)
//  val maxBounds = floatArrayOf(100f, 100f, 100f)
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
          (0 until vertices.size step 3).map { vertices[it] }.firstSortedBy { it } - padding,
          (1 until vertices.size step 3).map { vertices[it] }.firstSortedBy { it } - padding,
          (2 until vertices.size step 3).map { vertices[it] }.firstSortedBy { it } - padding
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
          (0 until vertices.size step 3).map { vertices[it] }.firstSortedByDescending { it } + padding,
          (1 until vertices.size step 3).map { vertices[it] }.firstSortedByDescending { it } + padding,
          (2 until vertices.size step 3).map { vertices[it] }.firstSortedByDescending { it } + padding
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

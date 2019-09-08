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

const val cellSize = 0.3f
const val cellHeight = 0.2f
const val agentHeight = 2f
const val agentRadius = 0.5f
const val agentMaxClimb = 0.3f

var originalNavMeshData: List<TriMesh> = listOf()

fun newNavMesh(deck: Deck): NavMesh {
  val elements = deck.architecture
  val meshes = newNavMeshTriMeshes(deck)
//  val vertices = meshes.flatMap { it.verts.toList() }
  originalNavMeshData = meshes

  val padding = 1f
  val minBounds = floatArrayOf(
      elements
          .map { deck.bodies[it.key]!!.position.x - deck.collisionShapes[it.key]!!.shape.radius }
          .firstSortedBy { it } - padding,
      elements
          .map { deck.bodies[it.key]!!.position.y - deck.collisionShapes[it.key]!!.shape.radius }
          .firstSortedBy { it } - padding,
      elements
          .map { deck.bodies[it.key]!!.position.z - deck.collisionShapes[it.key]!!.shape.radius }
          .firstSortedBy { it } - padding
//          (0 until vertices.size step 3).map { vertices[it] }.firstSortedBy { it },
//          (1 until vertices.size step 3).map { vertices[it] }.firstSortedBy { it },
//          (2 until vertices.size step 3).map { vertices[it] }.firstSortedBy { it }
  )

  val maxBounds = floatArrayOf(
      elements
          .map { deck.bodies[it.key]!!.position.x + deck.collisionShapes[it.key]!!.shape.radius }
          .firstSortedByDescending { it } + padding,
      elements
          .map { deck.bodies[it.key]!!.position.y + deck.collisionShapes[it.key]!!.shape.radius }
          .firstSortedByDescending { it } + padding,
      elements
          .map { deck.bodies[it.key]!!.position.z + deck.collisionShapes[it.key]!!.shape.radius }
          .firstSortedByDescending { it } + padding
//          (0 until vertices.size step 3).map { vertices[it] }.firstSortedByDescending { it },
//          (1 until vertices.size step 3).map { vertices[it] }.firstSortedByDescending { it },
//          (2 until vertices.size step 3).map { vertices[it] }.firstSortedByDescending { it }
  )
  val geometry = GeometryProvider(
      meshes.toMutableList(),
      mutableListOf(),
      minBounds,
      maxBounds
  )

  val recastConfig = RecastConfig(
      RecastConstants.PartitionType.WATERSHED,
      cellSize,
      cellHeight,
      agentHeight,
      agentRadius,
      agentMaxClimb,
      45f,
      8,
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

  val builderConfig = RecastBuilderConfig(recastConfig, minBounds, maxBounds)
  val builder = RecastBuilder()
  val buildResult = builder.build(geometry, builderConfig)
  val params = newNavMeshDataCreateParams(geometry, buildResult)
  val meshData = NavMeshBuilder.createNavMeshData(params)
  if (meshData == null)
    throw Error("Error generating NavMesh")

  return NavMesh(meshData, vertsPerPoly, 0)
}

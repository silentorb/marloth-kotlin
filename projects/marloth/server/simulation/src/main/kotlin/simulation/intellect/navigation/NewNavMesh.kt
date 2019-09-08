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

private data class IntermediateMesh(
    val vertices: List<Vector3>,
    val triangles: List<Int>
)

private val boxLength = 0.5f

private val boxVertices = listOf(
    Vector3(boxLength, boxLength, boxLength),
    Vector3(-boxLength, -boxLength, boxLength),
    Vector3(-boxLength, -boxLength, boxLength),
    Vector3(-boxLength, -boxLength, boxLength),

    Vector3(boxLength, boxLength, -boxLength),
    Vector3(-boxLength, boxLength, -boxLength),
    Vector3(-boxLength, -boxLength, -boxLength),
    Vector3(boxLength, -boxLength, -boxLength)
)

private fun square(a: Int, b: Int, c: Int, d: Int): List<Int> =
    listOf(a, b, c, a, c, d)

private fun box(halfExtents: Vector3) =
    IntermediateMesh(
        vertices = boxVertices.map { it * halfExtents },
        triangles = listOf(
            square(0, 1, 2, 3), //  top
            square(3, 2, 6, 7),
            square(2, 1, 5, 6),
            square(1, 0, 4, 5),
            square(0, 3, 7, 4),
            square(7, 6, 5, 4) // bottom
        ).flatten()
    )

private fun getShapeVertices(shape: Shape): IntermediateMesh =
    when (shape) {

      is Box -> box(shape.halfExtents)

//      is Cylinder -> IntermediateMesh(
//          vertices = listOf(
//              Vector3(),
//              Vector3(),
//              Vector3(),
//              Vector3(),
//              Vector3(),
//              Vector3(),
//              Vector3(),
//              Vector3()
//          ),
//          triangles = listOf()
//      )
      is Cylinder -> box(Vector3(shape.radius, shape.radius, shape.height))

      else -> throw Error("Not implemented")
    }

private val SAMPLE_POLYAREA_TYPE_WALKABLE = 0x3f
private val walkable = AreaModification(SAMPLE_POLYAREA_TYPE_WALKABLE)

private val vertsPerPoly = 6

private data class GeometryProvider(
    val _meshes: MutableIterable<TriMesh>,
    val _convexVolumes: MutableIterable<ConvexVolume>,
    val _meshBoundsMin: FloatArray,
    val _meshBoundsMax: FloatArray
) : InputGeomProvider {
  override fun meshes(): MutableIterable<TriMesh> = _meshes

  override fun convexVolumes(): MutableIterable<ConvexVolume> = _convexVolumes
  override fun getMeshBoundsMin(): FloatArray = _meshBoundsMin

  override fun getMeshBoundsMax(): FloatArray {
    return floatArrayOf(100f, 100f, 100f)
  }

}

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

private const val cellSize = 0.3f
private const val cellHeight = 0.2f
private const val agentHeight = 2f
private const val agentRadius = 0.5f
private const val agentMaxClimb = 0.3f

fun newNavMesh(deck: Deck): NavMesh {
  val elements = deck.architecture
  val meshes = elements.map { (id, _) ->
    val shape = deck.collisionShapes[id]!!.shape
    val body = deck.bodies[id]!!
    val mesh = getShapeVertices(shape)
    val transform = getBodyTransform(body)
    val vertices = mesh.vertices.flatMap {
      val temp = it.transform(transform)
      listOf(temp.x, temp.y, temp.z)
    }
        .toFloatArray()
    val faces = mesh.triangles.toIntArray()
    TriMesh(vertices, faces)
  }
  val vertices = meshes.flatMap { it.verts.toList() }

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

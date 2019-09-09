package simulation.intellect.navigation

import mythic.spatial.Vector3
import org.recast4j.detour.NavMesh
import org.recast4j.recast.AreaModification
import org.recast4j.recast.ConvexVolume
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

private val boxLength = 1f

private val boxVertices = listOf(
    Vector3(boxLength, boxLength, boxLength),
    Vector3(-boxLength, boxLength, boxLength),
    Vector3(-boxLength, -boxLength, boxLength),
    Vector3(boxLength, -boxLength, boxLength),

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
            square(4, 7, 6, 5) // bottom
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

val SAMPLE_POLYAREA_TYPE_WALKABLE = 0x3f
val walkable = AreaModification(SAMPLE_POLYAREA_TYPE_WALKABLE)

data class GeometryProvider(
    val _meshes: MutableIterable<TriMesh>,
    val _convexVolumes: MutableIterable<ConvexVolume>,
    val _meshBoundsMin: FloatArray,
    val _meshBoundsMax: FloatArray
) : InputGeomProvider {
  override fun meshes(): MutableIterable<TriMesh> = _meshes
  override fun convexVolumes(): MutableIterable<ConvexVolume> = _convexVolumes
  override fun getMeshBoundsMin(): FloatArray = _meshBoundsMin
  override fun getMeshBoundsMax(): FloatArray = _meshBoundsMax
}

fun newNavMeshTriMeshes(deck: Deck): List<TriMesh> {
  val elements = deck.architecture
  return elements.entries
//      .filter { !it.value.isWall }
//      .take(1)
      .map { (id, _) ->
        val shape = deck.collisionShapes[id]!!.shape
        val body = deck.bodies[id]!!
        val mesh = getShapeVertices(shape)
        val transform = getBodyTransform(body).scale(body.scale)
        val vertices = mesh.vertices.flatMap {
                    val temp = it.transform(transform)
          // Convert to an array and Recast's Y-up coordinate system
          listOf(temp.x, temp.z, temp.y)
        }
            .toFloatArray()
        val faces = mesh.triangles.toIntArray()
        TriMesh(vertices, faces)
      }
}

package simulation.intellect.navigation

import mythic.spatial.Vector3
import scenery.Box
import scenery.Cylinder
import scenery.Shape
import simulation.main.Deck

private data class IntermediateMesh(
    val vertices: List<Vector3>,
    val triangles: List<Byte>
)

private val boxLength = 0.5f

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

private fun getShapeVertices(shape: Shape): IntermediateMesh =
    when(shape) {

      is Box -> IntermediateMesh(
          vertices = boxVertices.map {it * shape.halfExtents},
          triangles = listOf()
      )

      is Cylinder -> IntermediateMesh(
          vertices = listOf(
              Vector3(),
              Vector3(),
              Vector3(),
              Vector3(),
              Vector3(),
              Vector3(),
              Vector3(),
              Vector3()
          ),
          triangles = listOf()
      )

      else -> throw Error("Not implemented")
    }

fun buildNavMesh(deck: Deck) {
  deck.architecture.forEach { id, _ ->
    val shape = deck.collisionShapes[id]!!.shape
    val body = deck.bodies[id]!!
    val mesh = getShapeVertices(shape)
  }
}

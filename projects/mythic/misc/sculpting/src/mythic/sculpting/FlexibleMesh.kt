package mythic.sculpting

import mythic.spatial.Vector3
import org.joml.minus

class FlexibleEdge(
    val first: Vector3,
    val second: Vector3,
    val face: FlexibleFace,
    var edges: MutableList<FlexibleEdge>
) {
  fun other(node: Vector3) = if (node === first) second else first
  fun toList() = listOf(first, second)
}

class FlexibleFace(
    val edges: MutableList<FlexibleEdge> = mutableListOf()
) {
  val vertices: List<Vector3>
    get() = edges.flatMap { it.toList() }.distinct()

  var normal = Vector3()

  fun updateNormal() {
    normal = (vertices[0] - vertices[1]).cross(vertices[2] - vertices[1]).normalize()
  }
}

class FlexibleMesh {
  //  val vertices: MutableList<Vector3> = mutableListOf()
  val edges: MutableList<FlexibleEdge> = mutableListOf()
  val faces: MutableList<FlexibleFace> = mutableListOf()

  val vertices: List<Vector3>
    get() = faces.flatMap { it.vertices }

//  fun addVertex(vertex: Vector3): Vector3 {
//    vertices.add(vertex)
//    return vertex
//  }

  fun createFace(): FlexibleFace {
    val face = FlexibleFace()
    faces.add(face)
    return face
  }

  fun createFace(initializer: List<Vector3>): FlexibleFace {
    val face = createFace()
    replaceFaceVertices(face, initializer)
    return face
  }

  fun getMatchingEdges(first: Vector3, second: Vector3) =
      edges.filter { existing ->
        (existing.first == first && existing.second == second)
            || (existing.first == second && existing.second == first)
      }

  fun createEdge(first: Vector3, second: Vector3, face: FlexibleFace): FlexibleEdge {
    val others = getMatchingEdges(first, second)
    val edge = FlexibleEdge(first, second, face, others.toMutableList())
    for (other in others) {
      other.edges.add(edge)
    }
    edges.add(edge)
    return edge
  }

  fun replaceFaceVertices(face: FlexibleFace, initializer: List<Vector3>) {
    var previous = initializer.last()
    for (vertex in initializer) {
      val edge = createEdge(previous, vertex, face)
      if (edge.edges.size > 0) {
        val k = 0
      }
      face.edges.add(edge)
      previous = vertex
    }
  }

}

fun calculateNormals(mesh: FlexibleMesh) {
  for (face in mesh.faces) {
    face.updateNormal()
  }
}
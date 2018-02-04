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
  val unorderedVertices: List<Vector3>
    get() = edges.flatMap { it.toList() }.distinct()

  val vertices: List<Vector3>
    get() = edges.map { it.first }

  var normal = Vector3()

  fun updateNormal() {
    normal = (unorderedVertices[0] - unorderedVertices[1]).cross(unorderedVertices[2] - unorderedVertices[1]).normalize()
  }
}

class FlexibleMesh {
  //  val vertices: MutableList<Vector3> = mutableListOf()
  val edges: MutableList<FlexibleEdge> = mutableListOf()
  val faces: MutableList<FlexibleFace> = mutableListOf()

  val vertices: List<Vector3>
    get() = faces.flatMap { it.unorderedVertices }

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
    var previous = initializer.first()
    for (next in initializer.drop(1)) {
      val edge = createEdge(previous, next, face)
      face.edges.add(edge)
      previous = next
    }
    face.edges.add(createEdge(initializer.last(), initializer.first(), face))
  }

}

fun calculateNormals(mesh: FlexibleMesh) {
  for (face in mesh.faces) {
    face.updateNormal()
  }
}
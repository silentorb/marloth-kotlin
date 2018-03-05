package mythic.sculpting

import mythic.spatial.Vector3
import org.joml.minus
import org.joml.plus

class FlexibleEdge(
    val first: Vector3,
    val second: Vector3,
    val face: FlexibleFace,
    val edges: MutableList<FlexibleEdge>
) {
  fun other(node: Vector3) = if (node === first) second else first
  fun toList() = listOf(first, second)

  val middle: Vector3
    get() = first + second / 2f
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
    get() = faces.flatMap { it.vertices }

  val distinctVertices: List<Vector3>
    get() = vertices.distinct()

  fun createFace(): FlexibleFace {
    val face = FlexibleFace()
    faces.add(face)
    return face
  }

  fun createFace(vertices: List<Vector3>): FlexibleFace {
    val face = createFace()
    replaceFaceVertices(face, vertices)
    return face
  }

  fun createStitchedFace(vertices: List<Vector3>): FlexibleFace {
    val face = createFace(vertices)
    stitchEdges(face.edges)
    return face
  }

  fun getMatchingEdges(first: Vector3, second: Vector3) =
      edges.filter { existing ->
        (existing.first == first && existing.second == second)
            || (existing.first == second && existing.second == first)
      }

  fun createEdge(first: Vector3, second: Vector3, face: FlexibleFace): FlexibleEdge {
    val edge = FlexibleEdge(first, second, face, mutableListOf())
    edges.add(edge)
    return edge
  }

  fun stitchEdges(edges: List<FlexibleEdge>) {
    for (edge in edges) {
      val others = getMatchingEdges(edge.first, edge.second)
      for (other in others) {
        if (other !== edge) {
          other.edges.add(edge)
          edge.edges.add(other)
        }
      }
    }
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

//  fun import(mesh: FlexibleMesh) {
//    val vertexMap = mesh.distinctVertices.associate { Pair(it, Vector3(it)) }
//    val faceMap = mesh.faces.associate { Pair(it, FlexibleFace()) }
//    val edgeMap = mesh.edges.associate { Pair(it, FlexibleEdge()) }
//  }

  fun sharedImport(mesh: FlexibleMesh) {
    faces.addAll(mesh.faces)
    edges.addAll(mesh.edges)
  }

}

fun calculateNormals(mesh: FlexibleMesh) {
  for (face in mesh.faces) {
    face.updateNormal()
  }
}
package mythic.sculpting

import mythic.spatial.Vector3

class FlexibleEdge(
    val first: Vector3,
    val second: Vector3,
    val faces: MutableList<FlexibleFace> = mutableListOf()
) {
  fun other(node: Vector3) = if (node === first) second else first
  fun toList() = listOf(first, second)
}

class FlexibleFace(
    val edges: MutableList<FlexibleEdge> = mutableListOf()
) {
  val vertices: List<Vector3>
    get() = edges.flatMap { it.toList() }.distinct()
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

  fun createEdge(first: Vector3, second: Vector3): FlexibleEdge {
    for (existing in edges) {
      if (existing.first == first && existing.second == second)
        return existing
    }
    val edge = FlexibleEdge(first, second)
    edges.add(edge)
    return edge
  }

  fun replaceFaceVertices(face: FlexibleFace, initializer: List<Vector3>) {
    var previous = initializer.first()
    for (vertex in initializer) {
      val edge = createEdge(vertex, previous)
//      addVertex(vertex)
      edge.faces.add(face)
      face.edges.add(edge)
      previous = vertex
    }
  }

}
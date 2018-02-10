package mythic.sculpting

import mythic.spatial.*

fun skinLoop(mesh: FlexibleMesh, first: List<Vector3>, second: List<Vector3>) {
  val sides = (0 until first.size).map { a ->
    val b = if (a == first.size - 1) 0 else a + 1
    mesh.createStitchedFace(listOf(
        first[b], first[a],
        second[a], second[b]
    ))
  }
}

fun skin(mesh: FlexibleMesh, first: List<Vector3>, second: List<Vector3>) {
  val sides = (0 until first.size - 1).map { a ->
    val b = a + 1
    mesh.createStitchedFace(
        if (a == 0 && first[a] == first[b])
          listOf(first[a], second[a], second[b])
        else if (a == first.size - 1 && second[a] == second[b])
          listOf(first[b], first[a], second[a])
        else
          listOf(first[b], first[a], second[a], second[b])
    )
  }
}

fun extrudeBasic(mesh: FlexibleMesh, face: FlexibleFace, transform: Matrix) {
  val newVertices = face.vertices
      .reversed()
      .map { it.transform(transform) }
  val secondFace = mesh.createFace(newVertices)
  val secondVertices = secondFace.vertices.reversed()
  skinLoop(mesh, face.vertices, secondVertices)
}

fun lathe(mesh: FlexibleMesh, vertices: List<Vector3>, count: Int, sweep: Float = Pi * 2) {
  val increment = sweep / count
  var previous = vertices
  for (i in 1 until count) {
    val next = vertices.map { it.transform(Matrix().rotateZ(i * increment)) }
    skin(mesh, previous, next)
    previous = next
  }
  skin(mesh, previous, vertices)
}

class operations {
  companion object {

    fun set_opposite_edge(mesh: HalfEdgeMesh, edge: HalfEdge) {
      val opposite = mesh.get_opposite_edge(edge.next!!.vertex, edge.vertex)
      if (opposite != null) {
        edge.opposite = opposite
        opposite.opposite = edge
      }
    }
  }
}

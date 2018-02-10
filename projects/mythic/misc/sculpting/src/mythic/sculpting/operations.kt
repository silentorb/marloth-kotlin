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
    if (first[a] == first[b]) {
      mesh.createStitchedFace(listOf(
          first[a],
          second[a], second[b]
      ))
    }
    else if (second[a] == second[b]) {
      mesh.createStitchedFace(listOf(
          first[b], first[a],
          second[a]
      ))
    }
    else {
      mesh.createStitchedFace(listOf(
          first[b], first[a],
          second[a], second[b]
      ))
    }
  }
}

fun extrudeBasic(mesh: FlexibleMesh, face: FlexibleFace, transform: Matrix) {
  val newVertices = face.vertices
      .reversed()
      .map { it.transform(transform) }
  val secondFace = mesh.createFace(newVertices)
  val secondVertices = secondFace.vertices.reversed()
  skinLoop(mesh, face.vertices, secondVertices)
//  val sides = (0 until face.vertices.size).map { a ->
//    val b = if (a > 2) 0 else a + 1
//    mesh.createStitchedFace(listOf(
//        face.vertices[b], face.vertices[a],
//        secondVertices[a], secondVertices[b]
//    ))
//  }
}

fun lathe(mesh: FlexibleMesh, vertices: List<Vector3>, count: Int, sweep: Float = Pi * 2) {
  val increment = sweep / count
  var previous = vertices
//  mesh.createFace(previous)
  for (i in 1 until count) {
    val next = vertices.map { it.transform(Matrix().rotateZ(i * increment)) }
//    mesh.createFace(next)
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

//
//    fun fill_parallel_edges(mesh: HalfEdgeMesh, face: HalfEdgeFace, first: HalfEdge, second: HalfEdge): HalfEdgeFace {
//      face.edge = first
//      first.face = face
//      second.face = face
//
//      fun create(a: HalfEdge, b: HalfEdge) {
//        val temp = create_edge(mesh, face, a.opposite!!.vertex, b)
//        a.next = temp
//        temp.previous = a
//      }
//
//      create(first, second)
//      create(second, first)
//
//      return face
//    }

  }
}

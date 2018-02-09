package mythic.sculpting

import mythic.sculpting.query.each_vertex
import mythic.sculpting.query.getEdges
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import mythic.spatial.times
import mythic.spatial.transform
import org.joml.plus
import java.util.*

fun extrudeBasic(mesh: FlexibleMesh, face: FlexibleFace, transform: Matrix) {
  val newVertices = face.vertices
      .reversed()
      .map { it.transform(transform) }
  val secondFace = mesh.createFace(newVertices)
  val secondVertices = secondFace.vertices.reversed()
  val sides = (0 until newVertices.size).map { a ->
    val b = if (a > 2) 0 else a + 1
    mesh.createStitchedFace(listOf(
        face.vertices[b], face.vertices[a],
        secondVertices[a], secondVertices[b]
    ))
  }
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

package mythic.sculpting

import mythic.spatial.Matrix
import mythic.spatial.Vector3
import mythic.spatial.plusAssign
import mythic.spatial.times
import java.util.*

class operations {
  companion object {

//    fun detach_face(face: Face) {
//      query.each_vertex(face, { vertex: Vertex -> vertex.edge.face = null })
//
//      face.edge = null
//    }

    fun set_opposite_edge(mesh: HalfEdgeMesh, edge: Edge) {
      val opposite = mesh.get_opposite_edge(edge.next!!.vertex, edge.vertex)
      if (opposite != null) {
        edge.opposite = opposite
        opposite.opposite = edge
      }
    }

    fun create_edge(mesh: HalfEdgeMesh, face: Face, vertex: Vertex, next: Edge): Edge {
      val edge = Edge(vertex, next, null, null, face)
      mesh.add_edge(edge)
      set_opposite_edge(mesh, edge)
      return edge
    }

    fun fill_parallel_edges(mesh: HalfEdgeMesh, face: Face, first: Edge, second: Edge): Face {
      face.edge = first
      first.face = face
      second.face = face

      fun create(a: Edge, b: Edge) {
        val temp = create_edge(mesh, face, a.opposite!!.vertex, b)
        a.next = temp
        temp.previous = a
      }

      create(first, second)
      create(second, first)

      return face
    }

    fun extrude_basic(mesh: HalfEdgeMesh, face: Face): ArrayList<Face> {
      val result = ArrayList<Face>()
//      val original_points = query.vertices(face)
      val new_points = clone_vertices(mesh, face)
      val original_edges = query.edges(face)

//      detach_face(face)
      mesh.replace_face_vertices(face, new_points)
      transform(face, Matrix().translate(Vector3(0f, 0f, -0.6f)))
      val new_edges = query.edges(face).listIterator()
      for (original_edge in original_edges) {
        val new_edge = new_edges.next()
        val new_face = mesh.create_face()
        val opposite_new_edge = Edge(new_edge.next!!.vertex, null, null, new_edge, face)
        mesh.add_edge(opposite_new_edge)
        new_edge.opposite = opposite_new_edge
        result.add(fill_parallel_edges(mesh, new_face, original_edge, opposite_new_edge))
      }

      return result
    }

    fun extrude_absolute(mesh: HalfEdgeMesh, face: Face, matrix: Matrix): ArrayList<Face> {
      val result = extrude_basic(mesh, face)
      transform(face, matrix)
      return result
    }

//    fun extrude_relative(mesh: HalfEdgeMesh, face: Face, matrix: sculpting.Matrix) {}

    fun clone_vertices(mesh: HalfEdgeMesh, face: Face): List<Vertex> {
      val result = Array<Vertex>(0, { Vertex(Vector3(0f, 0f, 0f)) })
      var i = 0
      query.each_vertex(face, { vertex: Vertex -> result[i++] = mesh.add_vertex(vertex) })
      return result.toList()
    }

//    fun flip(face: Face) {
//      var edge = face.edge!!.next
//      var previous = face.edge!!
//      while (edge !== face.edge) {
//        val temp = edge
//        edge = edge.next
//        temp.next = previous
//        previous = temp
//      }
//    }

    fun translate(face: Face, offset: Vector3) {
      query.each_vertex(face, { vertex: Vertex -> vertex.position += offset })
    }

    fun transform(face: Face, matrix: Matrix) {
      query.each_vertex(face, { vertex: Vertex -> vertex.position = Vector3(vertex.position * matrix) })
    }
  }
}

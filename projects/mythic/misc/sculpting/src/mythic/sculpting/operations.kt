package mythic.sculpting

import mythic.sculpting.query.each_vertex
import mythic.sculpting.query.getEdges
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import mythic.spatial.times
import org.joml.plus
import java.util.*

class operations {
  companion object {

//    fun detach_face(face: HalfEdgeFace) {
//      query.each_vertex(face, { vertex: HalfEdgeVertex -> vertex.edge.face = null })
//
//      face.edge = null
//    }

    fun set_opposite_edge(mesh: HalfEdgeMesh, edge: HalfEdge) {
      val opposite = mesh.get_opposite_edge(edge.next!!.vertex, edge.vertex)
      if (opposite != null) {
        edge.opposite = opposite
        opposite.opposite = edge
      }
    }

    fun create_edge(mesh: HalfEdgeMesh, face: HalfEdgeFace, vertex: HalfEdgeVertex, next: HalfEdge): HalfEdge {
      val edge = HalfEdge(vertex, next, null, null, face)
      mesh.add_edge(edge)
      set_opposite_edge(mesh, edge)
      return edge
    }

    fun fill_parallel_edges(mesh: HalfEdgeMesh, face: HalfEdgeFace, first: HalfEdge, second: HalfEdge): HalfEdgeFace {
      face.edge = first
      first.face = face
      second.face = face

      fun create(a: HalfEdge, b: HalfEdge) {
        val temp = create_edge(mesh, face, a.opposite!!.vertex, b)
        a.next = temp
        temp.previous = a
      }

      create(first, second)
      create(second, first)

      return face
    }

    fun extrude_basic(mesh: HalfEdgeMesh, face: HalfEdgeFace): ArrayList<HalfEdgeFace> {
      val result = ArrayList<HalfEdgeFace>()
//      val original_points = query.getVertices(face)
      val new_points = clone_vertices(mesh, face)
      val original_edges = getEdges(face)

//      detach_face(face)
      mesh.replaceFaceVertices(face, new_points)
      transform(face, Matrix().translate(Vector3(0f, 0f, -0.6f)))
      val new_edges = getEdges(face).listIterator()
      for (originalEdge in original_edges) {
        val newEdge = new_edges.next()
        val new_face = mesh.createFace()
        val opposite_new_edge = HalfEdge(newEdge.next!!.vertex, null, null, newEdge, face)
        mesh.add_edge(opposite_new_edge)
        newEdge.opposite = opposite_new_edge
        result.add(fill_parallel_edges(mesh, new_face, originalEdge, opposite_new_edge))
      }

      return result
    }

    fun extrude_absolute(mesh: HalfEdgeMesh, face: HalfEdgeFace, matrix: Matrix): ArrayList<HalfEdgeFace> {
      val result = extrude_basic(mesh, face)
      transform(face, matrix)
      return result
    }

//    fun extrude_relative(mesh: HalfEdgeMesh, face: HalfEdgeFace, matrix: sculpting.Matrix) {}

    fun clone_vertices(mesh: HalfEdgeMesh, face: HalfEdgeFace): List<HalfEdgeVertex> {
      val result = Array<HalfEdgeVertex>(0, { HalfEdgeVertex(Vector3(0f, 0f, 0f)) })
      var i = 0
      each_vertex(face, { vertex: HalfEdgeVertex -> result[i++] = mesh.addVertex(vertex) })
      return result.toList()
    }

//    fun flip(face: HalfEdgeFace) {
//      var edge = face.edge!!.next
//      var previous = face.edge!!
//      while (edge !== face.edge) {
//        val temp = edge
//        edge = edge.next
//        temp.next = previous
//        previous = temp
//      }
//    }

    fun translate(face: HalfEdgeFace, offset: Vector3) {
      each_vertex(face, { vertex: HalfEdgeVertex -> vertex.position += offset })
    }

    fun transform(face: HalfEdgeFace, matrix: Matrix) {
      each_vertex(face, { vertex: HalfEdgeVertex -> vertex.position = Vector3(vertex.position * matrix) })
    }
  }
}

package mythic.sculpting.query

import mythic.sculpting.Edge
import mythic.sculpting.Face
import mythic.sculpting.HalfEdgeMesh
import mythic.sculpting.Vertex
import mythic.spatial.BoundingBox
import mythic.spatial.Vector3

fun vertex_count(face: Face): Int {
  var result = 1
  var edge = face.edge!!.next

  while (edge !== face.edge) {
    edge = edge!!.next
    ++result
  }

  return result
}

fun vertex_count(mesh: HalfEdgeMesh): Int {
  var result = 0
  for (polygon in mesh.faces) {
    result += vertex_count(polygon)
  }
  return result
}

inline fun <T> each_edge(face: Face, action: (Edge) -> T) {
  var edge = face.edge
  do {
    action(edge!!)
    edge = edge.next
  } while (edge !== face.edge)
}

inline fun <T> each_vertex(face: Face, action: (Vertex) -> T) {
  each_edge(face, { action(it.vertex) })
}

fun getVertices(face: Face): List<Vertex> {
  val result = ArrayList<Vertex>()
  each_vertex(face, { result.add(it) })
  return result
}

fun edges(face: Face): List<Edge> {
  val result = ArrayList<Edge>()
  each_edge(face, { result.add(it) })
  return result
}

fun getBounds(vertices: List<Vertex>): BoundingBox {
  val v = vertices.map { it.position }
  return BoundingBox(
      Vector3(
          v.minBy { it.x }!!.x,
          v.minBy { it.y }!!.y,
          v.minBy { it.z }!!.z
      ),
      Vector3(
          v.maxBy { it.x }!!.x,
          v.maxBy { it.y }!!.y,
          v.maxBy { it.z }!!.z
      )
  )
}

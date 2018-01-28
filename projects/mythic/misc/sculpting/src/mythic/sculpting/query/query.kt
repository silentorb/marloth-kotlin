package mythic.sculpting.query

import mythic.sculpting.HalfEdge
import mythic.sculpting.HalfEdgeFace
import mythic.sculpting.HalfEdgeMesh
import mythic.sculpting.HalfEdgeVertex
import mythic.spatial.BoundingBox
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import org.joml.div
import org.joml.plus

fun getVertexCount(face: HalfEdgeFace): Int {
  var result = 1
  var edge = face.edge!!.next

  while (edge !== face.edge) {
    edge = edge!!.next
    ++result
  }

  return result
}

fun getVertexCount(mesh: HalfEdgeMesh): Int {
  var result = 0
  for (polygon in mesh.faces) {
    result += getVertexCount(polygon)
  }
  return result
}

inline fun <T> each_edge(face: HalfEdgeFace, action: (HalfEdge) -> T) {
  var edge = face.edge
  do {
    action(edge!!)
    edge = edge.next
  } while (edge !== face.edge)
}

inline fun <T> each_vertex(face: HalfEdgeFace, action: (HalfEdgeVertex) -> T) {
  each_edge(face, { action(it.vertex) })
}

fun getVertices(face: HalfEdgeFace): List<HalfEdgeVertex> {
  val result = ArrayList<HalfEdgeVertex>()
  each_vertex(face, { result.add(it) })
  return result
}

fun getEdges(face: HalfEdgeFace): List<HalfEdge> {
  val result = ArrayList<HalfEdge>()
  each_edge(face, { result.add(it) })
  return result
}

//fun getBounds(vertices: List<HalfEdgeVertex>): BoundingBox {
//  val v = vertices.map { it.position }
//  return BoundingBox(
//      Vector3(
//          v.minBy { it.x }!!.x,
//          v.minBy { it.y }!!.y,
//          v.minBy { it.z }!!.z
//      ),
//      Vector3(
//          v.maxBy { it.x }!!.x,
//          v.maxBy { it.y }!!.y,
//          v.maxBy { it.z }!!.z
//      )
//  )
//}

fun getCenter(points: List<Vector2>): Vector2 =
    points.reduce { a, b -> a + b } / points.size.toFloat()

fun getCenter(vertices: List<Vector3>): Vector3 {
  var result = Vector3()
  for (vertex in vertices) {
    result += vertex
  }
  return result / vertices.size.toFloat()
}

fun getBounds(vertices: List<Vector3>): BoundingBox {
  return BoundingBox(
      Vector3(
          vertices.minBy { it.x }!!.x,
          vertices.minBy { it.y }!!.y,
          vertices.minBy { it.z }!!.z
      ),
      Vector3(
          vertices.maxBy { it.x }!!.x,
          vertices.maxBy { it.y }!!.y,
          vertices.maxBy { it.z }!!.z
      )
  )
}


package mythic.sculpting.query

import mythic.spatial.BoundingBox
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import org.joml.div
import org.joml.plus

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

fun getCenter2D(points: List<Vector2>): Vector2 =
    points.reduce { a, b -> a + b } / points.size.toFloat()

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


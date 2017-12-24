package rendering

import org.joml.Vector2i
import org.joml.plus
import spatial.Matrix
import spatial.Quaternion
import spatial.Vector3
import spatial.times
import javax.swing.Spring.height


fun createViewMatrix(position: Vector3, orientation: Quaternion): Matrix {
  val forward = orientation * Vector3(1f, 0f, 0f)
  val look_at = position + forward
  val result = Matrix()
  result.setLookAt(position, look_at, Vector3(0f, 0f, 1f))
  return result
}

fun getAspectRatio(dimensions: Vector2i): Float {
  return dimensions.x.toFloat() / dimensions.y.toFloat()
}

fun createPerspectiveMatrix(dimensions: Vector2i, angle: Float = 45f): Matrix {
  val ratio = getAspectRatio(dimensions)
  val radians = Math.toRadians(angle.toDouble()).toFloat()
  return Matrix()
      .setPerspective(radians, ratio, 0.01f, 200.0f)
}
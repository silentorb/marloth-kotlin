package rendering

import mythic.spatial.Matrix
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import org.joml.*
import scenery.Camera

fun createViewMatrix(position: Vector3, orientation: Quaternion): Matrix {
  val temp = Quaternion()
  val forward = orientation * Vector3(1f, 0f, 0f)
  val look_at = position + forward
  return Matrix()
      .setLookAt(position, look_at, Vector3(0f, 0f, 1f))
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

fun createCameraMatrix(dimensions: Vector2i, camera: Camera): Matrix {
  val projection = createPerspectiveMatrix(dimensions, camera.angle)
  val view = createViewMatrix(camera.position, camera.orientation)
  return projection * view
}
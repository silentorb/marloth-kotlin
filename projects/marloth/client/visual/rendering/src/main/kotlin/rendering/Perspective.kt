package rendering

import mythic.spatial.Matrix
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import org.joml.*
import scenery.Camera
import scenery.ProjectionType

fun createViewMatrix(position: Vector3, orientation: Quaternion): Matrix {
  val forward = orientation * Vector3(1f, 0f, 0f)
  val look_at = position + forward
  return Matrix()
      .setLookAt(position, look_at, Vector3(0f, 0f, 1f))
}

fun getAspectRatio(dimensions: Vector2i): Float {
  return dimensions.x.toFloat() / dimensions.y.toFloat()
}

fun createPerspectiveMatrix(dimensions: Vector2i, angle: Float, nearClip: Float, farClip: Float): Matrix {
  val ratio = getAspectRatio(dimensions)
  val radians = Math.toRadians(angle.toDouble()).toFloat()
  return Matrix()
      .setPerspective(radians, ratio, nearClip, farClip)
}

fun createOrthographicMatrix(dimensions: Vector2i, zoom: Float, nearClip: Float, farClip: Float): Matrix {
  val ratio = getAspectRatio(dimensions)
  return Matrix()
      .setOrtho(-1f * zoom, 1f * zoom, -1f * zoom, 1f * zoom, nearClip, farClip)
}

fun createCameraMatrix(dimensions: Vector2i, camera: Camera): Matrix {
  val projection = if (camera.projectionType == ProjectionType.orthographic)
    createOrthographicMatrix(dimensions, camera.position.length(), camera.nearClip, camera.farClip)
  else
    createPerspectiveMatrix(dimensions, camera.angle, camera.nearClip, camera.farClip)

  val view = createViewMatrix(camera.position, camera.orientation)
  return projection * view
}

fun createCameraEffectsData(dimensions: Vector2i, camera: Camera) =
    CameraEffectsData(
        createCameraMatrix(dimensions, camera),
        camera.orientation * Vector3(1f, 0f, 0f)
    )
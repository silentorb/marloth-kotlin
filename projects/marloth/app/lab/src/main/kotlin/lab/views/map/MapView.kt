package lab.views.map

import haft.isActive
import lab.LabCommandType
import lab.views.LabCommandState
import mythic.bloom.Bounds
import mythic.platforming.WindowInfo
import mythic.spatial.*
import org.joml.Vector2i
import org.joml.minus
import org.joml.plus
import rendering.createCameraEffectsData
import scenery.Camera
import simulation.Realm

data class MapViewCamera(
    var target: Vector3 = Vector3(),
    var distance: Float = 20f,
    var yaw: Float = 0f
)

enum class MapViewDrawMode {
  solid,
  wireframe
}

data class MapViewDisplayConfig(
    var drawMode: MapViewDrawMode = MapViewDrawMode.solid,
    var normals: Boolean = false,
    var vertexIndices: Boolean = false
)

data class MapViewConfig(
    val camera: MapViewCamera = MapViewCamera(),
    val display: MapViewDisplayConfig = MapViewDisplayConfig(),
    var selection: List<Int> = listOf(),
    var tempStart: Vector3 = Vector3(),
    var tempEnd: Vector3 = Vector3(),
    var raySkip: Int = 0
)

data class Hit(
    val position: Vector3,
    val index: Int
)

private fun getFaceHits(start: Vector3, end: Vector3, world: Realm): List<Hit> {
  val faces = world.nodes.flatMap { it.faces }.distinct()
//      .take(1)
  val rayDirection = (end - start).normalize()

  return faces.mapIndexedNotNull { i, it ->
    if (it.normal.x == 0f && it.normal.y == 0f && it.normal.z == 0f)
      it.updateNormal()

    val point = rayIntersectsPolygon3D(start, rayDirection, it.vertices, it.normal)
    if (point != null)
      Hit(point, i)
    else
      null
  }
}

private fun castSelectionRay(config: MapViewConfig, camera: Camera, world: Realm, mousePosition: Vector2i, bounds: Bounds) {
  val dimensions = bounds.dimensions
  val cursor = mousePosition - bounds.position.toVector2i()
  val cameraData = createCameraEffectsData(dimensions.toVector2i(), camera)
  val viewportBounds = listOf(
      0, 0,
      bounds.dimensions.x.toInt(), bounds.dimensions.y.toInt()
  ).toIntArray()
  val start = cameraData.transform.unproject(cursor.x.toFloat(), bounds.dimensions.y - cursor.y.toFloat(), 0.01f, viewportBounds, Vector3())
  val end = start + cameraData.direction * camera.farClip
  config.tempStart = start
  config.tempEnd = end
}

private fun trySelect(config: MapViewConfig, world: Realm) {
  val start = config.tempStart
  val end = config.tempEnd
  config.tempStart = start
  config.tempEnd = end
  val hits = getFaceHits(start, end, world)
  if (hits.size > 0) {
    val sorted = hits.sortedBy { it.position.distance(start) }
    val index = config.raySkip % sorted.size
    val hit = sorted[index]
    config.selection = listOf(hit.index).toMutableList()
  } else {
    config.selection = mutableListOf()
  }
}

fun updateMapState(config: MapViewConfig, world: Realm, camera: Camera, input: LabCommandState, windowInfo: WindowInfo, delta: Float) {
  val commands = input.commands

  if (isActive(commands, LabCommandType.select)) {
    val bounds = Bounds(0f, 0f, windowInfo.dimensions.x.toFloat(), windowInfo.dimensions.y.toFloat())
    config.raySkip = 0
    castSelectionRay(config, camera, world, input.mousePosition, bounds)
    trySelect(config, world)
  }

  if (isActive(commands, LabCommandType.incrementRaySkip)) {
    ++config.raySkip
    trySelect(config, world)
  }

  if (isActive(commands, LabCommandType.decrementRaySkip)) {
    --config.raySkip
    trySelect(config, world)
  }

  val moveSpeed = 30
  val zoomSpeed = 120
  val rotateSpeed = 5

  val moveOffset = Vector3()
  if (isActive(commands, LabCommandType.moveUp))
    moveOffset.y = 1f

  if (isActive(commands, LabCommandType.moveDown))
    moveOffset.y = -1f

  if (isActive(commands, LabCommandType.moveLeft))
    moveOffset.x = -1f

  if (isActive(commands, LabCommandType.moveRight))
    moveOffset.x = 1f

  if (moveOffset != Vector3())
    config.camera.target += moveOffset.transform(Matrix().rotateZ(config.camera.yaw)) * (moveSpeed * delta)

  if (isActive(commands, LabCommandType.zoomIn))
    config.camera.distance -= zoomSpeed * delta

  if (isActive(commands, LabCommandType.zoomOut))
    config.camera.distance += zoomSpeed * delta

  if (isActive(commands, LabCommandType.rotateLeft))
    config.camera.yaw = (config.camera.yaw - (rotateSpeed * delta)) % (Pi * 2)

  if (isActive(commands, LabCommandType.rotateRight))
    config.camera.yaw = (config.camera.yaw + (rotateSpeed * delta)) % (Pi * 2)

  if (isActive(commands, LabCommandType.toggleMeshDisplay)) {
    config.display.drawMode = if (config.display.drawMode == MapViewDrawMode.wireframe)
      MapViewDrawMode.solid
    else
      MapViewDrawMode.wireframe
  }

  if (isActive(commands, LabCommandType.toggleNormals))
    config.display.normals = !config.display.normals

  if (isActive(commands, LabCommandType.toggleVertexIndices))
    config.display.vertexIndices = !config.display.vertexIndices

}
package lab.views.map

import haft.isActive
import lab.LabCommandType
import lab.views.LabCommandState
import mythic.bloom.Bounds
import mythic.ent.Id
import mythic.platforming.WindowInfo
import mythic.spatial.*
import org.joml.minus
import rendering.createCameraEffectsData
import simulation.Realm

data class MapViewCamera(
    var target: Vector3 = Vector3(),
    var distance: Float = 20f,
    var yaw: Float = 0f,
    var pitch: Float = Pi / 4f
)

enum class MapViewDrawMode {
  solid,
  wireframe
}

data class MapViewDisplayConfig(
    var drawMode: MapViewDrawMode = MapViewDrawMode.solid,
    var normals: Boolean = false,
    var faceIds: Boolean = false,
    var nodeIds: Boolean = false
)

data class MapViewConfig(
    val camera: MapViewCamera = MapViewCamera(),
    val display: MapViewDisplayConfig = MapViewDisplayConfig(),
    var selection: List<Id> = listOf(),
    var tempStart: Vector3 = Vector3(),
    var tempEnd: Vector3 = Vector3(),
    var raySkip: Int = 0
)

data class Hit(
    val position: Vector3,
    val id: Id
)

private fun getFaceHits(start: Vector3, end: Vector3, world: Realm): List<Hit> {
  val faces = world.nodeList.flatMap { it.faces }.distinct()
//      .take(1)
  val rayDirection = (end - start).normalize()

  return faces.mapNotNull { id ->
    val face = world.mesh.faces[id]!!
    if (face.normal.x == 0f && face.normal.y == 0f && face.normal.z == 0f)
      assert(false)
//      it.updateNormal()

    val point = rayIntersectsPolygon3D(start, rayDirection, face.vertices, face.normal)
    if (point != null)
      Hit(point, id)
    else
      null
  }
}

private fun castSelectionRay(config: MapViewConfig, world: Realm, mousePosition: Vector2, bounds: Bounds) {
  val camera = createTopDownCamera(config.camera)
  val dimensions = bounds.dimensions
  val cursor = mousePosition.toVector2i() - bounds.position
  val cameraData = createCameraEffectsData(dimensions, camera)
  val viewportBounds = listOf(
      0, 0,
      bounds.dimensions.x.toInt(), bounds.dimensions.y.toInt()
  ).toIntArray()
  val start = Vector3(cameraData.transform.unproject(cursor.x.toFloat(),
      bounds.dimensions.y - cursor.y.toFloat(), 0.01f, viewportBounds, Vector3m()))
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
    config.selection = listOf(hit.id)
  } else {
    config.selection = listOf()
  }
}

fun updateMapState(config: MapViewConfig, world: Realm, input: LabCommandState, windowInfo: WindowInfo, delta: Float) {
  val commands = input.commands

  if (isActive(commands, LabCommandType.select)) {
    val bounds = Bounds(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)
    config.raySkip = 0
    castSelectionRay(config, world, input.mousePosition, bounds)
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

  val moveSpeed = 50
  val zoomSpeed = 120
  val rotateSpeed = 5

  val moveOffset = Vector3(
      if (isActive(commands, LabCommandType.moveLeft))
        -1f
      else if (isActive(commands, LabCommandType.moveRight))
        1f
      else
        0f,
      if (isActive(commands, LabCommandType.moveUp))
        1f
      else if (isActive(commands, LabCommandType.moveDown))
        -1f
      else
        0f
  )

  val distanceOffset = config.camera.distance / 50f

  if (moveOffset != Vector3())
    config.camera.target += moveOffset.transform(Matrix().rotateZ(config.camera.yaw)) * (moveSpeed * delta * distanceOffset)

  if (isActive(commands, LabCommandType.zoomIn))
    config.camera.distance = Math.max(1f, config.camera.distance - zoomSpeed * delta * distanceOffset)

  val pitchRange = Pi / 2f - 0.001f
  if (isActive(commands, LabCommandType.rotateUp))
    config.camera.pitch = Math.min(pitchRange, config.camera.pitch + rotateSpeed * delta)

  if (isActive(commands, LabCommandType.rotateDown))
    config.camera.pitch = Math.max(-pitchRange, config.camera.pitch - rotateSpeed * delta)

  if (isActive(commands, LabCommandType.zoomOut))
    config.camera.distance = Math.min(200f, config.camera.distance + zoomSpeed * delta * distanceOffset)

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

  if (isActive(commands, LabCommandType.toggleFaceIds))
    config.display.faceIds = !config.display.faceIds

  if (isActive(commands, LabCommandType.toggleNodeIds))
    config.display.nodeIds = !config.display.nodeIds

}
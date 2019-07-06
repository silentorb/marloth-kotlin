package lab.views.map

import haft.HaftCommand
import haft.isActive
import lab.LabCommandType
import lab.views.LabCommandState
import mythic.bloom.BloomState
import mythic.bloom.Bounds
import mythic.bloom.selectedMenuValue
import mythic.ent.Id
import mythic.platforming.WindowInfo
import mythic.spatial.*
import org.joml.minus
import org.joml.times
import simulation.physics.joinInputVector
import simulation.physics.playerMoveMap
import rendering.createCameraEffectsData
import simulation.Command
import simulation.CommandType
import simulation.Realm

data class Hit(
    val position: Vector3,
    val id: Id
)

private fun getFaceHits(start: Vector3, end: Vector3, world: Realm): List<Hit> {
  val faces = world.nodeList.flatMap { it.faces }.distinct()
//      .take(1)
  val rayDirection = (end - start).normalize()

  return listOf()
//  return faces.mapNotNull { id ->
////    val face = world.mesh.faces[id]!!
////    if (face.normal.x == 0f && face.normal.y == 0f && face.normal.z == 0f)
////      assert(false)
////      it.updateNormal()
//
//    if (id == 434L) {
//      val k = 0
//    }
//    val point = rayIntersectsPolygon3D(start, rayDirection, face.vertices, face.normal)
//    if (point != null)
//      Hit(point, id)
//    else
//      null
//  }
}

private fun castSelectionRay(config: MapViewConfig, world: Realm, mousePosition: Vector2, bounds: Bounds) {
  val camera = createMapViewCamera(config)

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

private var lastHits: List<Id> = listOf()

private fun trySelect(config: MapViewConfig, world: Realm) {
  val start = config.tempStart
  val end = config.tempEnd
  config.tempStart = start
  config.tempEnd = end
  val hits = getFaceHits(start, end, world)
  if (hits.size > 0) {
    val sorted = hits.sortedBy { it.position.distance(start) }.map { it.id }
    val hit = sorted[config.raySkip % sorted.size]
    val isSame = lastHits == sorted
    lastHits = sorted
    if (!isSame)
      config.raySkip = 0

    if (config.selection.contains(hit)) {
      config.raySkip = (config.raySkip + 1) % sorted.size
      val hit2 = sorted[config.raySkip % sorted.size]
      config.selection = listOf(hit2)
    } else
      config.selection = listOf(hit)
  } else {
    config.selection = listOf()
  }
}

private fun switchCameraMode(config: MapViewConfig) {
  config.cameraMode = if (config.cameraMode == MapViewCameraMode.orbital)
    MapViewCameraMode.firstPerson
  else
    MapViewCameraMode.orbital
}

fun applyMapStateCommand(config: MapViewConfig, world: Realm, command: LabCommandType) {
  when (command) {
    LabCommandType.incrementRaySkip -> trySelect(config, world)

    LabCommandType.decrementRaySkip -> {
      --config.raySkip
      trySelect(config, world)
    }

    LabCommandType.toggleMeshDisplay -> config.display.solid = !config.display.solid

    LabCommandType.toggleWireframe -> config.display.wireframe = !config.display.wireframe

    LabCommandType.toggleNormals -> config.display.normals = !config.display.normals

    LabCommandType.toggleFaceIds -> config.display.faceIds = !config.display.faceIds

    LabCommandType.toggleNodeIds -> config.display.nodeIds = !config.display.nodeIds

    LabCommandType.toggleIsolateSelection -> config.display.isolateSelection = !config.display.isolateSelection

    LabCommandType.toggleAbstract -> config.display.abstract = !config.display.abstract

    LabCommandType.switchCamera -> switchCameraMode(config)

    else -> {
    }
  }
}

fun updateOrbitalCamera(camera: MapViewOrbitalCamera, commands: List<HaftCommand<LabCommandType>>, delta: Float) {
  val zoomSpeed = 200
  val moveSpeed = 50
  val rotateSpeed = 2

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

  val distanceOffset = camera.distance / 50f

  if (moveOffset != Vector3())
    camera.target += moveOffset.transform(Matrix().rotateZ(camera.yaw)) * (moveSpeed * delta * distanceOffset)

  if (isActive(commands, LabCommandType.zoomIn))
    camera.distance = Math.max(1f, camera.distance - zoomSpeed * delta * distanceOffset)

  val pitchRange = Pi / 2f - 0.001f
  if (isActive(commands, LabCommandType.rotateUp))
    camera.pitch = Math.min(pitchRange, camera.pitch + rotateSpeed * delta)

  if (isActive(commands, LabCommandType.rotateDown))
    camera.pitch = Math.max(-pitchRange, camera.pitch - rotateSpeed * delta)

  if (isActive(commands, LabCommandType.zoomOut))
    camera.distance = Math.min(200f, camera.distance + zoomSpeed * delta * distanceOffset)

  if (isActive(commands, LabCommandType.rotateLeft))
    camera.yaw = (camera.yaw - (rotateSpeed * delta)) % (Pi * 2)

  if (isActive(commands, LabCommandType.rotateRight))
    camera.yaw = (camera.yaw + (rotateSpeed * delta)) % (Pi * 2)
}

fun updateFirstPersonCamera(camera: MapViewFirstPersonCamera, commands: List<HaftCommand<LabCommandType>>, delta: Float) {
  val moveSpeed = 50f
  val rotateSpeed = 2

  val playerCommands = commands.mapNotNull {
    when (it.type) {
      LabCommandType.moveLeft -> CommandType.moveLeft
      LabCommandType.moveRight -> CommandType.moveRight
      LabCommandType.moveDown -> CommandType.moveDown
      LabCommandType.moveUp -> CommandType.moveUp
      else -> null
    }
  }.map { Command(type = it, target = 0, value = 1f) }

  val moveOffset = joinInputVector(playerCommands, playerMoveMap)

  if (moveOffset != null) {
    val rotation = Quaternion().rotateZ(camera.yaw - Pi / 2).rotateX(-camera.pitch)
    val offset = rotation * moveOffset * delta * moveSpeed
//    val rotationMatrix = Matrix().rotateZ(camera.yaw).rotateY(camera.pitch)
    camera.position += offset
  }

  val pitchRange = Pi / 2f - 0.001f
  if (isActive(commands, LabCommandType.rotateUp))
    camera.pitch = Math.max(-pitchRange, camera.pitch - rotateSpeed * delta)

  if (isActive(commands, LabCommandType.rotateDown))
    camera.pitch = Math.min(pitchRange, camera.pitch + rotateSpeed * delta)

  if (isActive(commands, LabCommandType.rotateLeft))
    camera.yaw = (camera.yaw + (rotateSpeed * delta)) % (Pi * 2)

  if (isActive(commands, LabCommandType.rotateRight))
    camera.yaw = (camera.yaw - (rotateSpeed * delta)) % (Pi * 2)
}

fun updateMapState(config: MapViewConfig, world: Realm, input: LabCommandState, windowInfo: WindowInfo,
                   bloomState: BloomState, delta: Float) {
  val menuCommandType = selectedMenuValue<LabCommandType>(bloomState.bag)

  val commands = input.commands

  val command = commands.firstOrNull()?.type ?: menuCommandType
  if (command != null) {
    applyMapStateCommand(config, world, command)
  }

  if (bloomState.bag[bagClickMap] != null) {
    val bounds = Bounds(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)
    castSelectionRay(config, world, input.mousePosition, bounds)
    trySelect(config, world)
  }


  if (config.cameraMode == MapViewCameraMode.orbital) {
    updateOrbitalCamera(config.orbitalCamera, commands, delta)
  } else {
    updateFirstPersonCamera(config.firstPersonCamera, commands, delta)
  }
}

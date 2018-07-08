package lab.views.map

import haft.isActive
import lab.LabCommandType
import lab.views.LabInputState
import mythic.spatial.*
import org.joml.plus
import simulation.ViewMode

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
    val display: MapViewDisplayConfig = MapViewDisplayConfig()
)

fun updateMapState(config: MapViewConfig, input: LabInputState, delta: Float) {
  val commands = input.commands

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
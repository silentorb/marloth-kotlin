package lab.views.map

import haft.isActive
import lab.LabCommandType
import lab.views.LabInputState
import mythic.spatial.*
import simulation.ViewMode

data class MapViewCamera(
    val target: Vector3 = Vector3(),
    var distance: Float = 20f
)

enum class MapViewDrawMode {
  solid,
  wireframe
}

data class MapViewDisplayConfig(
    var drawMode: MapViewDrawMode = MapViewDrawMode.solid
)

data class MapViewConfig(
    val camera: MapViewCamera = MapViewCamera(),
    val display: MapViewDisplayConfig = MapViewDisplayConfig()
)

fun updateMapState(config: MapViewConfig, input: LabInputState, delta: Float) {
  val commands = input.commands

//  if (isActive(commands, LabCommandType.toggleMeshDisplay)) {
//    config.displayMode = if (config.displayMode == GameDisplayMode.normal)
//      GameDisplayMode.wireframe
//    else
//      GameDisplayMode.normal
//  }
  val moveSpeed = 15
  val zoomSpeed = 120
  if (isActive(commands, LabCommandType.moveUp))
    config.camera.target.y += moveSpeed * delta

  if (isActive(commands, LabCommandType.moveDown))
    config.camera.target.y -= moveSpeed * delta

  if (isActive(commands, LabCommandType.moveLeft))
    config.camera.target.x -= moveSpeed * delta

  if (isActive(commands, LabCommandType.moveRight))
    config.camera.target.x += moveSpeed * delta

  if (isActive(commands, LabCommandType.zoomIn))
    config.camera.distance -= zoomSpeed * delta

  if (isActive(commands, LabCommandType.zoomOut))
    config.camera.distance += zoomSpeed * delta

  if (isActive(commands, LabCommandType.toggleMeshDisplay)) {
    config.display.drawMode = if (config.display.drawMode == MapViewDrawMode.wireframe)
      MapViewDrawMode.solid
    else
      MapViewDrawMode.wireframe
  }
}
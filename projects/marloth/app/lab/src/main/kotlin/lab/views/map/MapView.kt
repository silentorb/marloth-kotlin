package lab.views.map

import haft.isActive
import lab.LabCommandType
import lab.views.InputState

data class MapViewConfig(
    val dummy: Int = 0 // Not used.  Just here until other properties are added.
)

fun updateMapState(config: MapViewConfig, input: InputState, delta: Float) {
  val commands = input.commands

//  if (isActive(commands, LabCommandType.toggleMeshDisplay)) {
//    config.displayMode = if (config.displayMode == GameDisplayMode.normal)
//      GameDisplayMode.wireframe
//    else
//      GameDisplayMode.normal
//  }
}
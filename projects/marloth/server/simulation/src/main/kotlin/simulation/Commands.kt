package simulation

import mythic.ent.Id

enum class CommandType {
  none,

  lookLeft,
  lookRight,
  lookUp,
  lookDown,
  
  cameraLookLeft,
  cameraLookRight,
  cameraLookUp,
  cameraLookDown,

  moveUp,
  moveDown,
  moveLeft,
  moveRight,

  jump,
  attack,
  duck,
  run,

  switchView,
  joinGame
}

data class Command(
    val type: CommandType,
    val target: Id,
    val value: Float = 1f
)

typealias Commands = List<Command>
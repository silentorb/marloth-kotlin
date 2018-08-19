package simulation

enum class CommandType {
  none,

  lookLeft,
  lookRight,
  lookUp,
  lookDown,

  lookCameraUp,
  lookCameraDown,

  moveUp,
  moveDown,
  moveLeft,
  moveRight,

//  attackUp,
//  attackDown,
//  attackLeft,
//  attackRight,

  jump,
  attack,
  duck,
  run,

  switchView,

  activateDevice,
  joinGame,
  menu,
  menuSelect,
  menuBack,
}

data class Command(
    val type: CommandType,
    val target: Id,
    val value: Float = 1f
)

typealias Commands = List<Command>
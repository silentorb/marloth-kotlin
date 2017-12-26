package commanding

enum class CommandType {
  none,

  lookLeft,
  lookRight,
  lookUp,
  lookDown,

  moveUp,
  moveDown,
  moveLeft,
  moveRight,

  jump,
  attack,
  duck,
  run,

  menuBack,
  select,
}

data class Command(val type: CommandType, val target: Int, val value: Float)

typealias Commands = Array<Command>

typealias CommandSource = () -> Commands

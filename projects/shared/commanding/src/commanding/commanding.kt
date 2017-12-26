package commanding


enum class CommandType {
  none,

  look_left,
  look_right,
  look_up,
  look_down,

  move_forward,
  move_backward,
  move_left,
  move_right,

  jump,
  attack,
  duck,
  run,

  back_menu,
  select,
}

data class Command(val type: CommandType, val value: Float)

typealias Commands = Array<Command>

typealias CommandSource = () -> Commands

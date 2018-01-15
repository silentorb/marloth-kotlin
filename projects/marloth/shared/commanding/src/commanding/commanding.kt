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

  switchView,
  toggleLab,
  toggleAbstractView,
  toggleStructureView,

  menuBack,
  select,
}

enum class CommandLifetime {
  pressed,
  end
}

data class Command(
    val type: CommandType,
    val target: Int,
    val value: Float,
    val lifetime: CommandLifetime
)

typealias Commands = List<Command>

typealias CommandSource = () -> Commands

typealias CommandHandler = (Command) -> Unit

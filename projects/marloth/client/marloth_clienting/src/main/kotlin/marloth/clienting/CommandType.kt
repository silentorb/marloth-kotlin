package marloth.clienting

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

  activateDevice,
  joinGame,
  menu,
  menuSelect,
  menuBack,

  newGame,
  quit,

}

private fun standardStrokes() = setOf(
    CommandType.switchView,
    CommandType.activateDevice,
    CommandType.joinGame,
    CommandType.menu,
    CommandType.menuSelect,
    CommandType.menuBack,
    CommandType.newGame,
    CommandType.quit
)

val clientCommandStrokes = mapOf(
    BindingContext.game to standardStrokes(),
    BindingContext.menu to standardStrokes()
)
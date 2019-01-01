package marloth.clienting

enum class CommandType {
  none,

  equipSlot0,
  equipSlot1,
  equipSlot2,
  equipSlot3,

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
    CommandType.equipSlot0,
    CommandType.equipSlot1,
    CommandType.equipSlot2,
    CommandType.equipSlot3,
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
        .plus(setOf(
            CommandType.lookLeft,
            CommandType.lookRight,
            CommandType.lookUp,
            CommandType.lookDown,
            CommandType.moveUp,
            CommandType.moveDown,
            CommandType.moveLeft,
            CommandType.moveRight
        ))
)
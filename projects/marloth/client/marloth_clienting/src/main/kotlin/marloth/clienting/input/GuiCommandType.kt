package marloth.clienting.input

import simulation.input.CommandType

enum class GuiCommandType {
  characterInfo,
  menu,
  menuSelect,
  menuBack,
  newGame,
  quit
}

private fun standardClientStrokes() = setOf(
    GuiCommandType.characterInfo,
    GuiCommandType.menu,
    GuiCommandType.menuSelect,
    GuiCommandType.menuBack,
    GuiCommandType.newGame,
    GuiCommandType.quit
)

val clientCommandStrokes = mapOf(
    InputContext.game to standardClientStrokes(),
    InputContext.menu to standardClientStrokes()
        .plus(setOf(
            CommandType.moveUp,
            CommandType.moveDown,
            CommandType.moveLeft,
            CommandType.moveRight
        ))
)

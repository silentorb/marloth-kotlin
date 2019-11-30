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

val clientCommandStrokes =
    standardClientStrokes()
        .plus(setOf(
            CommandType.moveUp,
            CommandType.moveDown,
            CommandType.moveLeft,
            CommandType.moveRight
        ))

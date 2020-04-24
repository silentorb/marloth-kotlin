package marloth.clienting.input

import marloth.scenery.enums.CharacterCommands

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
    mapOf(
        InputContext.game to standardClientStrokes(),
        InputContext.menu to standardClientStrokes()
            .plus(setOf(
                CharacterCommands.moveUp,
                CharacterCommands.moveDown,
                CharacterCommands.moveLeft,
                CharacterCommands.moveRight
            ))
    )

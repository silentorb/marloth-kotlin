package marloth.clienting.input

import silentorb.mythic.happenings.CommonCharacterCommands

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
                CommonCharacterCommands.moveUp,
                CommonCharacterCommands.moveDown,
                CommonCharacterCommands.moveLeft,
                CommonCharacterCommands.moveRight
            ))
    )

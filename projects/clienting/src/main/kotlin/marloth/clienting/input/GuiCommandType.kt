package marloth.clienting.input

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.characters.targeting.toggleTargetingCommand

enum class GuiCommandType {
  characterInfo,
  menu,
  menuSelect,
  menuBack,
  newGame,
  quit
}

private fun standardStrokes() = setOf(
    GuiCommandType.characterInfo,
    GuiCommandType.menu,
    GuiCommandType.menuSelect,
    GuiCommandType.menuBack,
    GuiCommandType.newGame,
    GuiCommandType.quit
)

val commandStrokes =
    mapOf(
        InputContext.game to standardStrokes()
            .plus(setOf(
                toggleTargetingCommand
            )),
        InputContext.menu to standardStrokes()
            .plus(setOf(
                CharacterCommands.moveUp,
                CharacterCommands.moveDown,
                CharacterCommands.moveLeft,
                CharacterCommands.moveRight
            ))
    )

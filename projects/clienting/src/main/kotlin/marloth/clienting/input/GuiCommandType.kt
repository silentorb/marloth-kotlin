package marloth.clienting.input

import marloth.clienting.gui.hud.toggleTargetingCommand
import marloth.scenery.enums.CharacterCommands
import marloth.scenery.enums.CharacterRigCommands

enum class GuiCommandType {
  characterInfo,
  editor,
  menu,
  menuBack,
  menuSelect,
  mouseClick,
  mouseMove,
  newGame,
  quit
}

private fun standardStrokes() = setOf(
    GuiCommandType.characterInfo,
    GuiCommandType.editor,
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
                toggleTargetingCommand,
                CharacterRigCommands.switchView
            )),
        InputContext.menu to standardStrokes()
            .plus(setOf(
                CharacterCommands.moveUp,
                CharacterCommands.moveDown,
                CharacterCommands.moveLeft,
                CharacterCommands.moveRight
            ))
    )

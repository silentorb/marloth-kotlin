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
  mouseMove,
  newGame,
  tabNext,
  tabPrevious,
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
            .plus(
                setOf(
                    toggleTargetingCommand,
                    CharacterRigCommands.switchView,
                    CharacterCommands.abilityUtility,
                    CharacterCommands.abilityDefense,
                    CharacterCommands.abilityMobility,
                )
            ),
        InputContext.menu to standardStrokes()
            .plus(setOf(
                CharacterCommands.moveUp,
                CharacterCommands.moveDown,
                CharacterCommands.moveLeft,
                CharacterCommands.moveRight,
                GuiCommandType.tabPrevious,
                GuiCommandType.tabNext,
            ))
    )

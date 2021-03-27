package marloth.clienting.input

import marloth.clienting.gui.hud.toggleTargetingCommand
import marloth.scenery.enums.CharacterCommands
import marloth.scenery.enums.CharacterRigCommands
import silentorb.mythic.ent.reflectProperties

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

object DebugCommands {
  val toggleValue1 = "toggleValue1"
  val toggleValue2 = "toggleValue2"
  val toggleValue3 = "toggleValue3"
  val toggleValue4 = "toggleValue4"
}

private fun standardStrokes() = setOf(
    GuiCommandType.characterInfo,
    GuiCommandType.editor,
    GuiCommandType.menu,
    GuiCommandType.menuSelect,
    GuiCommandType.menuBack,
    GuiCommandType.newGame,
    GuiCommandType.quit
) + reflectProperties<String>(DebugCommands).toSet()

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

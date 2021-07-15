package marloth.clienting.input

import marloth.clienting.ClientEventType
import marloth.clienting.gui.hud.toggleTargetingCommand
import marloth.scenery.enums.CharacterCommands
import marloth.scenery.enums.CharacterRigCommands
import silentorb.mythic.ent.reflectProperties

object GuiCommandType {
  val characterInfo = "characterInfo"
  val menu = "menu"
  val menuSelect = "menuSelect"
  val mouseMove = "mouseMove"
  val newGame = "newGame"
  val tabNext = "tabNext"
  val tabPrevious = "tabPrevious"
  val quit = "quit"
}

object DeveloperCommands {
  val toggleValue1 = "toggleValue1"
  val toggleValue2 = "toggleValue2"
  val toggleValue3 = "toggleValue3"
  val toggleValue4 = "toggleValue4"
  val editor = "editor"
  val aura = "aura"
}

val developerCommands = reflectProperties<String>(DeveloperCommands)

private fun standardStrokes() = setOf(
    GuiCommandType.characterInfo,
    DeveloperCommands.editor,
    GuiCommandType.menu,
    GuiCommandType.menuSelect,
    ClientEventType.menuBack,
    GuiCommandType.newGame,
    GuiCommandType.quit
) + reflectProperties<String>(DeveloperCommands).toSet()

val commandStrokes =
    mapOf(
        InputContext.game to standardStrokes()
            .plus(
                setOf(
                    toggleTargetingCommand,
                    CharacterRigCommands.switchView,
                    CharacterCommands.interactPrimary,
                    CharacterCommands.stopInteracting,
                    CharacterCommands.useItem,
                    CharacterCommands.abilityDefense,
                    CharacterCommands.abilityMobility,
                    CharacterCommands.previousItem,
                    CharacterCommands.nextItem,
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

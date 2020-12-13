package marloth.clienting.gui.menus.logic

import marloth.clienting.ClientEventType
import marloth.clienting.GuiState
import marloth.clienting.gui.ViewId
import marloth.clienting.input.GuiCommandType
import marloth.scenery.enums.ClientCommand
import silentorb.mythic.bloom.OffsetBox
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.handleCommands
import simulation.main.Deck

fun nextView(stack: MenuStack) = handleCommands<ViewId?> { command, view ->
  when (command.type) {
    GuiCommandType.menu -> {
      if (view != null)
        null
      else
        ViewId.mainMenu
    }

    GuiCommandType.characterInfo -> {
      if (view == null)
        ViewId.characterStatus
      else
        null
    }

    ClientEventType.menuBack ->
      stack.lastOrNull()?.view

    ClientEventType.menuReplace -> command.value as? ViewId

    GuiCommandType.newGame -> null

    ClientEventType.navigate, ClientEventType.drillDown -> {
      if (command.value == null)
        null
      else
        command.value as? ViewId ?: view
    }

    view == ViewId.chooseProfessionMenu -> null

    else -> view
  }
}

fun selectInteractionView(deck: Deck?, player: Id): ViewId? =
    if (deck == null)
      null
    else {
      val interactingWith = deck.characters[player]?.interactingWith
      val interaction = deck.interactables[interactingWith]
      when (interaction?.primaryCommand?.clientCommand) {
        ClientCommand.showConversationView -> ViewId.conversation
        else -> null
      }
    }

fun updateMenuFocusIndex(state: GuiState, menuSize: Int?, commands: Commands, hoverBoxes: List<OffsetBox>) =
    if (menuSize != null) {
      val hoverFocusIndex = if (commands.any { it.type == GuiCommandType.mouseMove })
        getHoverIndex(hoverBoxes)
      else
        null

      updateMenuFocus(state.menuStack, menuSize, hoverFocusIndex)(commands, state.menuFocusIndex)
    } else
      0

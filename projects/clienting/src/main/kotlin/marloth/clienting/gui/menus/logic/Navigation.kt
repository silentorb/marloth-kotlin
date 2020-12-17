package marloth.clienting.gui.menus.logic

import marloth.clienting.ClientEventType
import marloth.clienting.gui.GuiState
import marloth.clienting.gui.ViewId
import marloth.clienting.input.GuiCommandType
import marloth.scenery.enums.ClientCommand
import silentorb.mythic.bloom.OffsetBox
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
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

fun getInteractionCommands(deck: Deck?, player: Id): Commands =
    if (deck == null)
      listOf()
    else {
      val interactingWith = deck.characters[player]?.interactingWith
      val interaction = deck.interactables[interactingWith]
      when (interaction?.primaryCommand?.commandType) {
        ClientCommand.showConversationView -> listOf(Command(type = ClientEventType.navigate, target = player, value = ViewId.conversation))
        else -> listOf()
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

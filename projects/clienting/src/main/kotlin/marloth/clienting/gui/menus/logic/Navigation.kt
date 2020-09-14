package marloth.clienting.gui.menus.logic

import marloth.clienting.GuiState
import marloth.clienting.input.GuiCommandType
import marloth.clienting.gui.ViewId
import silentorb.mythic.bloom.OffsetBox
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.HaftCommands
import simulation.main.Deck

fun nextView(stack: MenuStack, commands: HaftCommands, view: ViewId?): ViewId? {
  val commandTypes = commands.map { it.type }
  return when {
    commandTypes.contains(GuiCommandType.menu) -> {
      if (view != null)
        null
      else
        ViewId.mainMenu
    }

    commandTypes.contains(GuiCommandType.characterInfo) -> {
      if (view == ViewId.characterInfo)
        null
      else if (view == null)
        ViewId.characterInfo
      else
        null
    }

    commandTypes.contains(GuiCommandType.menuBack) -> stack.lastOrNull()?.view

    commandTypes.contains(GuiCommandType.newGame) -> null

    commandTypes.contains(GuiCommandType.navigate) -> {
      val command = commands.first { it.type == GuiCommandType.navigate }
      val destination = command.value
      if (destination is ViewId)
        destination
      else
        view
    }

    else -> view
  }
}

fun fallBackMenus(deck: Deck, player: Id): ViewId? =
    if (!deck.characters.containsKey(player))
      ViewId.chooseProfessionMenu
    else
      null

fun updateMenuFocusIndex(state: GuiState, menuSize: Int?, commandTypes: List<Any>, hoverBoxes: List<OffsetBox>) =
    if (menuSize != null) {
      val hoverFocusIndex = if (commandTypes.contains(GuiCommandType.mouseMove))
        getHoverIndex(hoverBoxes)
      else
        null

      updateMenuFocus(state.menuStack, menuSize, commandTypes, hoverFocusIndex, state.menuFocusIndex)
    } else
      0

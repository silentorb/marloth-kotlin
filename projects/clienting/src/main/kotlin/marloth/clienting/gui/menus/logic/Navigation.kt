package marloth.clienting.gui.menus.logic

import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.GuiState
import marloth.clienting.input.GuiCommandType
import marloth.clienting.gui.ViewId
import silentorb.mythic.bloom.OffsetBox
import silentorb.mythic.ent.Id
import simulation.main.Deck

fun getMenuReplaceView(events: List<Any>) =
    events
        .filterIsInstance<ClientEvent>()
        .firstOrNull {
          it.type == ClientEventType.menuReplace
        }?.data as? ViewId

fun nextView(stack: MenuStack, eventTypes: List<Any>, events: List<Any>, view: ViewId?): ViewId? {
  return when {
    eventTypes.contains(GuiCommandType.menu) -> {
      if (view != null)
        null
      else
        ViewId.mainMenu
    }

    eventTypes.contains(GuiCommandType.characterInfo) -> {
      if (view == null)
        ViewId.characterInfo
      else
        null
    }

    eventTypes.contains(ClientEventType.menuBack) -> stack.lastOrNull()?.view

    eventTypes.contains(ClientEventType.menuReplace) -> getMenuReplaceView(events)

    eventTypes.contains(GuiCommandType.newGame) -> null

    eventTypes.contains(ClientEventType.navigate) -> {
      val command = events.filterIsInstance<ClientEvent>().firstOrNull { it.type == ClientEventType.navigate }
      command?.data as? ViewId ?: view
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

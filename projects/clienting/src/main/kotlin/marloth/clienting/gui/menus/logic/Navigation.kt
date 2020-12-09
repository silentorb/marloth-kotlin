package marloth.clienting.gui.menus.logic

import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.GuiState
import marloth.clienting.input.GuiCommandType
import marloth.clienting.gui.ViewId
import marloth.scenery.enums.ClientCommand
import silentorb.mythic.bloom.OffsetBox
import silentorb.mythic.ent.Id
import simulation.main.Deck

fun getMenuReplaceView(events: List<Any>) =
    events
        .filterIsInstance<ClientEvent>()
        .firstOrNull {
          it.type == ClientEventType.menuReplace
        }?.value as? ViewId

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
        ViewId.characterStatus
      else
        null
    }

    eventTypes.contains(ClientEventType.menuBack) ->
      stack.lastOrNull()?.view

    eventTypes.contains(ClientEventType.menuReplace) -> getMenuReplaceView(events)

    eventTypes.contains(GuiCommandType.newGame) -> null

    eventTypes.contains(ClientEventType.navigate) -> {
      val command = events.filterIsInstance<ClientEvent>().firstOrNull { it.type == ClientEventType.navigate }
      command?.value as? ViewId ?: view
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

fun updateMenuFocusIndex(state: GuiState, menuSize: Int?, commandTypes: List<Any>, hoverBoxes: List<OffsetBox>) =
    if (menuSize != null) {
      val hoverFocusIndex = if (commandTypes.contains(GuiCommandType.mouseMove))
        getHoverIndex(hoverBoxes)
      else
        null

      updateMenuFocus(state.menuStack, menuSize, commandTypes, hoverFocusIndex, state.menuFocusIndex)
    } else
      0

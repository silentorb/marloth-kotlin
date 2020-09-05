package marloth.clienting.menus

import marloth.clienting.MarlothBloomState
import marloth.clienting.MarlothBloomStateMap
import marloth.clienting.PlayerViews
import marloth.clienting.input.GuiCommandType
import marloth.clienting.newMarlothBloomState
import silentorb.mythic.bloom.BloomState
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.HaftCommands
import simulation.main.Deck

fun lowerView(stack: MenuStack): MenuLayer? =
    stack.dropLast(1).lastOrNull()

fun nextView(command: Any?, view: ViewId?): ViewId? =
    when (command) {
      GuiCommandType.menu -> {
        if (view != null)
          null
        else
          ViewId.mainMenu
      }

      GuiCommandType.characterInfo -> {
        if (view == ViewId.characterInfo)
          null
        else if (view == null)
          ViewId.characterInfo
        else
          null
      }

      GuiCommandType.menuBack -> {
        if (view != null)
          null
        else
          null
      }

      GuiCommandType.newGame -> null

      else -> view
    }

fun fallBackMenus(deck: Deck, player: Id): ViewId? =
    if (!deck.characters.containsKey(player))
      ViewId.chooseProfessionMenu
    else
      null

fun updateMarlothBloomState(state: MarlothBloomState, events: HaftCommands): MarlothBloomState {
  val command = events.firstOrNull()
  return state.copy(
      view = nextView(command?.type, state.view)
  )
}

fun updateClientCurrentMenus(deck: Deck, bloomStates: MarlothBloomStateMap, events: HaftCommands, players: List<Id>): MarlothBloomStateMap {
  val narrowedEvents = events.filter { it.type != GuiCommandType.menuSelect }
  return players
      .associateWith { player ->
        val playerEvents = narrowedEvents.filter { it.target == player }
        updateMarlothBloomState(bloomStates[player] ?: newMarlothBloomState(), playerEvents)
//        val navigate = playerEvents.firstOrNull { it.type == GuiCommandType.navigate }
//        val view = playerViews[player] ?: listOf()
//        val bag = bloomStates[player]?.bag ?: mapOf()
//        if (navigate != null)
//          view + MenuLayer(view = navigate.value!! as ViewId, focusIndex = getMenuFocusIndex(bag))
//        else {
//          val command = playerEvents.firstOrNull()
//          if (command != null) {
//            nextView(command.type, view)
//          } else
//            view
//        }

//    when (manuallyChangedView) {
//      null, ViewId.none, ViewId.chooseProfessionMenu -> fallBackMenus(deck, player)
//      else -> manuallyChangedView
//    }
      }
}

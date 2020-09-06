package marloth.clienting.menus

import marloth.clienting.MarlothBloomState
import marloth.clienting.MarlothBloomStateMap
import marloth.clienting.PlayerViews
import marloth.clienting.input.GuiCommandType
import marloth.clienting.newMarlothBloomState
import silentorb.mythic.bloom.BloomState
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.HaftCommand
import silentorb.mythic.haft.HaftCommands
import simulation.main.Deck

fun lowerView(stack: MenuStack): MenuLayer? =
    stack.dropLast(1).lastOrNull()

fun nextView(stack: MenuStack, command: HaftCommand, view: ViewId?): ViewId? =
    when (command.type) {
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

      GuiCommandType.menuBack -> stack.lastOrNull()?.view

      GuiCommandType.newGame -> null

      GuiCommandType.navigate -> {
        val destination = command.value
        if (destination is ViewId)
          destination
        else
          view
      }

      else -> view
    }

fun updateMenuStack(command: HaftCommand, state: MarlothBloomState): MenuStack {
  val stack = state.menuStack
  return when (command.type) {
    GuiCommandType.navigate -> stack + MenuLayer(state.view!!, state.menuFocusIndex)
    GuiCommandType.menuBack -> stack.dropLast(1)
    GuiCommandType.menu -> listOf()
    else -> stack
  }
}

fun fallBackMenus(deck: Deck, player: Id): ViewId? =
    if (!deck.characters.containsKey(player))
      ViewId.chooseProfessionMenu
    else
      null

fun updateMarlothBloomState(state: MarlothBloomState, bloomDefinition: BloomDefinition, events: HaftCommands): MarlothBloomState {
  val command = events.firstOrNull()
  val menuSize = bloomDefinition.menu?.size
  return if (command == null)
    state
  else
    state.copy(
        view = nextView(state.menuStack, command, state.view),
        menuFocusIndex = if (menuSize != null) updateMenuFocus(state.menuStack, menuSize, command, state.menuFocusIndex) else 0,
        menuStack = updateMenuStack(command, state)
    )
}

fun updateClientCurrentMenus(deck: Deck, bloomStates: MarlothBloomStateMap,
                             playerBloomDefinitions: Map<Id, BloomDefinition>,
                             events: HaftCommands, players: List<Id>): MarlothBloomStateMap {
  val narrowedEvents = events.filter { it.type != GuiCommandType.menuSelect }
  return players
      .associateWith { player ->
        val playerEvents = narrowedEvents.filter { it.target == player }
        val state = bloomStates[player] ?: newMarlothBloomState()
        val bloomDefinition = playerBloomDefinitions[player] ?: newBloomDefinition(mapOf())
        updateMarlothBloomState(state, bloomDefinition, playerEvents)
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

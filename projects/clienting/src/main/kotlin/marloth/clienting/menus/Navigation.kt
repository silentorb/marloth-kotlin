package marloth.clienting.menus

import marloth.clienting.MarlothBloomState
import marloth.clienting.MarlothBloomStateMap
import marloth.clienting.input.GuiCommandType
import marloth.clienting.newMarlothBloomState
import silentorb.mythic.bloom.next.Box
import silentorb.mythic.bloom.next.flattenAllBoxes
import silentorb.mythic.bloom.next.getHoverBoxes
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.HaftCommand
import silentorb.mythic.haft.HaftCommands
import silentorb.mythic.spatial.Vector2i
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

fun fallBackMenus(deck: Deck, player: Id): ViewId? =
    if (!deck.characters.containsKey(player))
      ViewId.chooseProfessionMenu
    else
      null

fun updateMarlothBloomState(
    state: MarlothBloomState,
    bloomDefinition: BloomDefinition,
    hoverBoxes: List<Box>,
    events: HaftCommands
): MarlothBloomState {
  val command = events.firstOrNull()
  val menuSize = bloomDefinition.menu?.size
  val menuFocusIndex = if (menuSize != null) {
    val hoverFocusIndex = getHoverIndex(hoverBoxes)
    hoverFocusIndex ?: updateMenuFocus(state.menuStack, menuSize, command, state.menuFocusIndex)
  } else
    0

  return if (command == null)
    state.copy(
        menuFocusIndex = menuFocusIndex
    )
  else
    state.copy(
        view = nextView(state.menuStack, command, state.view),
        menuFocusIndex = menuFocusIndex,
        menuStack = updateMenuStack(command, state)
    )
}

fun updateClientCurrentMenus(deck: Deck, bloomStates: MarlothBloomStateMap,
                             playerBloomDefinitions: Map<Id, BloomDefinition>,
                             mousePosition: Vector2i,
                             boxes: Map<Id, Box>,
                             events: HaftCommands, players: List<Id>): MarlothBloomStateMap {
  val narrowedEvents = events.filter { it.type != GuiCommandType.menuSelect }
  return players
      .associateWith { player ->
        val playerEvents = narrowedEvents.filter { it.target == player }
        val state = bloomStates[player] ?: newMarlothBloomState()
        val bloomDefinition = playerBloomDefinitions[player]
        if (bloomDefinition == null)
          state
        else {
          val hoverBoxes = getHoverBoxes(mousePosition, flattenAllBoxes(boxes[player]!!))
          updateMarlothBloomState(state, bloomDefinition, hoverBoxes, playerEvents)
        }
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

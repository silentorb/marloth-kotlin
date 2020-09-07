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

fun updateMarlothBloomState(
    state: MarlothBloomState,
    bloomDefinition: BloomDefinition,
    hoverBoxes: List<Box>,
    commands: HaftCommands
): MarlothBloomState {
  val menuSize = bloomDefinition.menu?.size
  val commandTypes = commands.map { it.type }
  val menuFocusIndex = if (menuSize != null) {
    val hoverFocusIndex = getHoverIndex(hoverBoxes)
    hoverFocusIndex ?: updateMenuFocus(state.menuStack, menuSize, commandTypes, state.menuFocusIndex)
  } else
    0

  return if (commands.none())
    state.copy(
        menuFocusIndex = menuFocusIndex
    )
  else
    state.copy(
        view = nextView(state.menuStack, commands, state.view),
        menuFocusIndex = menuFocusIndex,
        menuStack = updateMenuStack(commandTypes, state)
    )
}

fun updateClientCurrentMenus(deck: Deck, bloomStates: MarlothBloomStateMap,
                             playerBloomDefinitions: Map<Id, BloomDefinition>,
                             mousePosition: Vector2i,
                             boxes: Map<Id, Box>,
                             events: HaftCommands, players: List<Id>): MarlothBloomStateMap {
  return players
      .associateWith { player ->
        val playerEvents = events.filter { it.target == player }
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

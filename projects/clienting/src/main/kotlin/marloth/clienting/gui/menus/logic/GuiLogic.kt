package marloth.clienting.gui.menus.logic

import marloth.clienting.*
import marloth.clienting.gui.BloomDefinition
import silentorb.mythic.bloom.OffsetBox
import silentorb.mythic.bloom.getHoverBoxes
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.HaftCommands
import silentorb.mythic.spatial.Vector2i

fun updateGuiState(
    options: AppOptions,
    state: GuiState,
    bloomDefinition: BloomDefinition,
    hoverBoxes: List<OffsetBox>,
    commands: HaftCommands,
    events: List<ClientEvent>
): GuiState {
  val menuSize = bloomDefinition.menu?.size
  val commandTypes = commands.map { it.type }
  val menuFocusIndex = updateMenuFocusIndex(state, menuSize, commandTypes, hoverBoxes)

  return if (commands.none())
    state.copy(
        menuFocusIndex = menuFocusIndex
    )
  else
    state.copy(
        view = nextView(state.menuStack, commands, state.view),
        menuFocusIndex = menuFocusIndex,
        menuStack = updateMenuStack(commandTypes, state),
        displayChange = updateDisplayChangeState(options.display, state, events)
    )
}

fun updateGuiStates(
    options: AppOptions,
    bloomStates: GuiStateMap,
    playerBloomDefinitions: Map<Id, BloomDefinition>,
    mousePosition: Vector2i,
    boxes: PlayerBoxes,
    commands: HaftCommands,
    events: List<ClientEvent>): GuiStateMap {
  return playerBloomDefinitions
      .mapValues { (player, bloomDefinition) ->
        val playerEvents = commands.filter { it.target == player }
        val state = bloomStates[player] ?: newMarlothBloomState()
        val hoverBoxes = getHoverBoxes(mousePosition, boxes[player]!!)
        updateGuiState(options, state, bloomDefinition, hoverBoxes, playerEvents, events)
      }
}

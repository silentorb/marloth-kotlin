package marloth.clienting.gui.menus.logic

import marloth.clienting.*
import marloth.clienting.gui.BloomDefinition
import marloth.clienting.gui.ViewId
import marloth.clienting.input.GuiCommandType
import silentorb.mythic.bloom.OffsetBox
import silentorb.mythic.bloom.getHoverBoxes
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.HaftCommand
import silentorb.mythic.haft.HaftCommands
import silentorb.mythic.spatial.Vector2i

fun eventsFromGuiState(state: GuiState): List<ClientEvent> {
  val timeout = state.displayChange?.timeout
  return if (timeout != null && timeout <= 0.0)
    listOf(
        ClientEvent(ClientEventType.revertDisplayChanges),
        ClientEvent(ClientEventType.menuBack)
    )
  else
    listOf()
}

fun commandToClientEvents(options: AppOptions, state: GuiState, command: HaftCommand): List<ClientEvent> =
    when (command.type) {
      GuiCommandType.menuBack -> {
        if (needsWindowChange(options.display, state.displayChange?.options))
          listOf(ClientEvent(ClientEventType.menuReplace, ViewId.displayChangeConfirmation))
        else
          listOf(ClientEvent(ClientEventType.menuBack))
      }
      else -> listOf()
    }

fun commandsToClientEvents(options: AppOptions, state: GuiState, commands: HaftCommands): List<ClientEvent> =
    commands.flatMap { commandToClientEvents(options, state, it) }

fun updateGuiState(
    options: AppOptions,
    state: GuiState,
    bloomDefinition: BloomDefinition,
    hoverBoxes: List<OffsetBox>,
    commands: HaftCommands,
    events: List<ClientEvent>
): GuiState {
  val menuSize = bloomDefinition.menu?.size
  val commandTypes = commands.map { it.type } + events.map { it.type }
  val menuFocusIndex = updateMenuFocusIndex(state, menuSize, commandTypes, hoverBoxes)
  val displayChange = updateDisplayChangeState(options.display, state, events)

  if (getDebugBoolean("LOG_CLIENT_EVENTS")) {
    for (event in events) {
      println("${event.type} ${event.data}")
    }
  }
  return if (commandTypes.none())
    state.copy(
        menuFocusIndex = menuFocusIndex,
        displayChange = displayChange
    )
  else
    state.copy(
        view = nextView(state.menuStack, commandTypes, events, state.view),
        menuFocusIndex = menuFocusIndex,
        menuStack = updateMenuStack(commandTypes, state),
        displayChange = displayChange
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
        updateGuiState(options, state, bloomDefinition, hoverBoxes, playerEvents, events.filter { it.user == player })
      }
}

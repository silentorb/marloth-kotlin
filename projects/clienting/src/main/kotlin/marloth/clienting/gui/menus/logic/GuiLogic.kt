package marloth.clienting.gui.menus.logic

import marloth.clienting.*
import marloth.clienting.gui.BloomDefinition
import marloth.clienting.gui.ViewId
import marloth.clienting.input.GuiCommandType
import silentorb.mythic.bloom.OffsetBox
import silentorb.mythic.bloom.getHoverBoxes
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.spatial.Vector2i
import simulation.main.Deck

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

fun commandToClientEvents(options: AppOptions, state: GuiState, command: Command): List<ClientEvent> =
    when (command.type) {
      GuiCommandType.menuBack -> {
        if (needsWindowChange(options.display, state.displayChange?.options))
          listOf(ClientEvent(ClientEventType.menuReplace, ViewId.displayChangeConfirmation))
        else
          listOf(ClientEvent(ClientEventType.menuBack))
      }
      else -> listOf()
    }

fun commandsToClientEvents(options: AppOptions, state: GuiState, commands: Commands): List<ClientEvent> =
    commands.flatMap { commandToClientEvents(options, state, it) }

fun updateGuiState(
    options: AppOptions,
    deck: Deck?,
    state: GuiState,
    bloomDefinition: BloomDefinition,
    hoverBoxes: List<OffsetBox>,
    commands: Commands,
    events: List<ClientEvent>,
    player: Id
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

  return state.copy(
      view = nextView(state.menuStack, commandTypes, events, state.view) ?: fallBackMenus(deck, player),
      menuFocusIndex = menuFocusIndex,
      menuStack = updateMenuStack(commandTypes, state),
      displayChange = displayChange
  )
}

fun updateGuiState(
    options: AppOptions,
    deck: Deck?,
    bloomStates: GuiStateMap,
    mousePosition: Vector2i,
    boxes: PlayerBoxes,
    commands: Commands,
    events: List<ClientEvent>,
    player: Id, bloomDefinition: BloomDefinition): GuiState {
  val playerEvents = commands.filter { it.target == player }
  val state = bloomStates[player] ?: newMarlothBloomState()
  val hoverBoxes = getHoverBoxes(mousePosition, boxes[player]!!)
  return updateGuiState(options, deck, state, bloomDefinition, hoverBoxes, playerEvents, events.filter { it.user == player }, player)
}

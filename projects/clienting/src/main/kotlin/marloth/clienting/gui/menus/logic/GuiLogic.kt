package marloth.clienting.gui.menus.logic

import marloth.clienting.*
import marloth.clienting.gui.BloomDefinition
import marloth.clienting.gui.ViewId
import marloth.clienting.input.GuiCommandType
import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.bloom.OffsetBox
import silentorb.mythic.bloom.old.getHoverBoxes
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.platforming.Devices
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

fun updatePrimaryDeviceMode(commands: Commands, primarydeviceMode: DeviceMode): DeviceMode =
    when {
      commands.any { it.device == Devices.keyboard || it.device == Devices.mouse } -> DeviceMode.mouseKeyboard
      commands.any { it.device >= Devices.gamepadFirst } -> DeviceMode.gamepad
      else -> primarydeviceMode
    }

fun updateGuiState(
    options: AppOptions,
    state: GuiState,
    bloomDefinition: BloomDefinition,
    hoverBoxes: List<OffsetBox>,
    commands: Commands,
): GuiState {
  val menuSize = bloomDefinition.menu?.size
  val commandTypes = commands.map { it.type }
  val menuFocusIndex = updateMenuFocusIndex(state, menuSize, commandTypes, hoverBoxes)
  val displayChange = updateDisplayChangeState(options.display, state, commands)

  if (getDebugBoolean("LOG_CLIENT_EVENTS")) {
    for (event in commands) {
      println("${event.type} ${event.value}")
    }
  }

  return state.copy(
      view = nextView(state.menuStack, commandTypes, commands, state.view),
      menuFocusIndex = menuFocusIndex,
      menuStack = updateMenuStack(commandTypes, state),
      displayChange = displayChange,
      primarydeviceMode = updatePrimaryDeviceMode(commands, state.primarydeviceMode),
  )
}

fun updateGuiState(
    options: AppOptions,
    deck: Deck?,
    bloomStates: GuiStateMap,
    mousePosition: Vector2i,
    boxes: PlayerBoxes,
    commands: Commands,
    player: Id, bloomDefinition: BloomDefinition): GuiState {
  val playerCommands = commands.filter { it.target == player }
  val state = bloomStates[player]
      ?: newMarlothBloomState(if (bloomStates.none()) DeviceMode.mouseKeyboard else DeviceMode.gamepad)

  val gameCommands = if (playerCommands.any { it.type == CharacterCommands.interactPrimary }) {
    val nextView = selectInteractionView(deck, player)
    if (nextView != null)
      listOf(Command(type = ClientEventType.navigate, target = player, value = nextView))
    else
      listOf()
  } else
    listOf()

  val hoverBoxes = getHoverBoxes(mousePosition, boxes[player]!!)
  return updateGuiState(options, state, bloomDefinition, hoverBoxes, playerCommands + gameCommands)
}

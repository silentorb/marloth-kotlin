package marloth.clienting.gui.menus.logic

import marloth.clienting.*
import marloth.clienting.gui.*
import marloth.clienting.input.GuiCommandType
import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.bloom.LogicInput
import silentorb.mythic.bloom.OffsetBox
import silentorb.mythic.bloom.old.getHoverBoxes
import silentorb.mythic.bloom.updateBloomLogic
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.InputDeviceState
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.platforming.Devices
import silentorb.mythic.spatial.toVector2i
import simulation.happenings.updateNotifications
import simulation.main.Deck
import simulation.updating.simulationDelta

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
    boxes: List<OffsetBox>,
    commands: Commands,
    deviceStates: List<InputDeviceState>
): GuiState {
  val menuSize = bloomDefinition.menu?.size
  val hoverBoxes = getHoverBoxes(deviceStates.last().mousePosition.toVector2i(), boxes)
  val menuFocusIndex = updateMenuFocusIndex(state, menuSize, commands, hoverBoxes)
  val displayChange = updateDisplayChangeState(options.display, state, commands)

  if (getDebugBoolean("LOG_CLIENT_EVENTS")) {
    for (event in commands) {
      println("${event.type} ${event.value}")
    }
  }

  val bloomLogicInput = LogicInput(
      state = state.bloomState,
      deviceStates = deviceStates,
  )

  return state.copy(
      view = nextView(state.menuStack)(commands, state.view),
      menuFocusIndex = menuFocusIndex,
      menuStack = updateMenuStack(state)(commands, state.menuStack),
      displayChange = displayChange,
      primarydeviceMode = updatePrimaryDeviceMode(commands, state.primarydeviceMode),
      notifications = updateNotifications(simulationDelta, commands, state.notifications),
      bloomState = updateBloomLogic(bloomLogicInput, boxes)
  )
}

fun updateGuiState(
    options: AppOptions,
    deck: Deck?,
    bloomStates: GuiStateMap,
    boxes: PlayerBoxes,
    commands: Commands,
    player: Id,
    bloomDefinition: BloomDefinition,
    deviceStates: List<InputDeviceState>): GuiState {
  val playerCommands = commands.filter { it.target == player }
  val state = bloomStates[player]
      ?: newGuiState(if (bloomStates.none()) DeviceMode.mouseKeyboard else DeviceMode.gamepad)

  val gameCommands = if (playerCommands.any { it.type == CharacterCommands.interactPrimary })
    getInteractionCommands(deck, player)
  else
    listOf()

  return updateGuiState(options, state, bloomDefinition, boxes[player]!!, playerCommands + gameCommands, deviceStates)
}

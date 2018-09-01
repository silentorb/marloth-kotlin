package marloth.clienting

import haft.*
import mythic.platforming.PlatformInput
import org.joml.Vector2i
import org.joml.minus
import scenery.GameScene

val gamepadSlotStart = 2

typealias UserCommand = HaftCommand<CommandType>

typealias UserCommands = List<UserCommand>

data class GeneralInputDeviceState<CT>(
    val profileStates: ProfileStates<CT>,
    val mousePosition: Vector2i
)

data class GeneralCommandState<CT>(
    val commands: List<HaftCommand<CT>>,
    val mousePosition: Vector2i,
    val mouseOffset: Vector2i
)

typealias CommandState = GeneralCommandState<CommandType>

typealias InputDeviceState = GeneralInputDeviceState<CommandType>

data class GameInputConfig(
    var mouseInput: Boolean = true
)

data class InputState(
    val device: InputDeviceState,
    val config: GameInputConfig
)

fun newInputState() =
    InputDeviceState(
        profileStates = mapOf(),
        mousePosition = Vector2i()
    )

fun initialGameInputState(): ProfileStates<CommandType> = mapOf()
//        , (1..maxPlayerCount).map { null }
//)

fun updateGamepadSlots(input: PlatformInput, previousMap: GamepadSlots): GamepadSlots =
    updateGamepadSlots(input.getGamepads().map { it.id }, previousMap)

fun selectGamepadHandler(GamepadInputSource: MultiDeviceScalarInputSource, gamepad: Int?, isActive: Boolean) =
    if (gamepad != null && isActive)
      { trigger: Int -> GamepadInputSource(gamepad, trigger) }
    else
      disconnectedScalarInputSource

fun getWaitingDevices(gamepadAssignments: MutableMap<Int, Int>, gamepads: List<GamepadDeviceId>) =
    gamepads.filter { d -> !gamepadAssignments.any { it.key == d } }

fun createDeviceHandlers(input: PlatformInput, gamepadAssignments: MutableMap<Int, Int>): List<ScalarInputSource> {
  return listOf(
      input.KeyboardInputSource,
      input.MouseInputSource
  )
      .plus(gamepadAssignments.map {
        { trigger: Int -> input.GamepadInputSource(it.key, trigger) }
      })
      .plus((1..(maxPlayerCount - gamepadAssignments.size)).map {
        disconnectedScalarInputSource
      })
}

data class PlayerInputProfile(
    val player: Int,
    val gameBindings: Bindings<CommandType>,
    val menuBindings: Bindings<CommandType>
)

data class AvailableInputProfiles(
    val playerInputProfiles: List<PlayerInputProfile>
)

fun selectActiveInputProfiles(playerInputProfiles: List<PlayerInputProfile>, players: List<Int>) =
    playerInputProfiles.filter { players.contains(it.player) }
        .map { it.gameBindings }

data class InputArguments(
    val deviceHandlers: List<ScalarInputSource>,
    val waitingDevices: List<GamepadDeviceId>,
    val previousState: InputDeviceState,
    val players: List<Int>
)

val playerInputProfiles = defaultGameInputProfiles()
val menuInputProfiles = defaultMenuInputProfiles()

// TODO: Make gamepadAssignments a part of InputState
val gamepadAssignments: MutableMap<Int, Int> = mutableMapOf()

//class ClientInput(val input: PlatformInput, val config: GameInputConfig) {

fun checkForNewGamepads1(arguments: InputArguments): ProfileStates<CommandType> {
  val (deviceHandlers, waitingDevices, previousState, players) = arguments
  val profiles = createWaitingGamepadProfiles(waitingDevices.size, gamepadAssignments.size)
  return gatherProfileCommands2(profiles, previousState.profileStates, deviceHandlers)
}

fun checkForNewGamepads2(events: ProfileStates<CommandType>, playerCount: Int): HaftCommands<CommandType> {
  val commands = gatherProfileCommands3(events)
  if (commands.size > 0)
    println(commands.size)
  var playerCounter = playerCount
  val keystrokes = filterKeystrokeCommands(commands, listOf(CommandType.activateDevice, CommandType.joinGame))
  for (command in keystrokes) {
    gamepadAssignments[command.target - 10] = playerCounter++
  }
  return commands
}

fun updateGameInput1(arguments: InputArguments, clientState: ClientState): ProfileStates<CommandType> {
  val playerInputProfiles = if (clientState.menu.isVisible)
    menuInputProfiles
  else
    playerInputProfiles

  val profiles = selectActiveInputProfiles(playerInputProfiles, arguments.players)
  return gatherProfileCommands2(profiles, arguments.previousState.profileStates, arguments.deviceHandlers)
}

fun applyMouseAxis(value: Int, firstCommandType: CommandType, secondCommandType: CommandType, scale: Float) =
    if (value > 0)
      listOf(HaftCommand(firstCommandType, 1, value.toFloat() * scale, TriggerLifetime.pressed))
    else if (value < 0)
      listOf(HaftCommand(secondCommandType, 1, -value.toFloat() * scale, TriggerLifetime.pressed))
    else
      listOf()

fun applyMouseMovement(arguments: InputArguments): HaftCommands<CommandType> {
  val position = arguments.mousePosition - arguments.previousState.mousePosition
  return listOf<HaftCommand<CommandType>>()
      .plus(applyMouseAxis(position.x, CommandType.lookRight, CommandType.lookLeft, 1f))
      .plus(applyMouseAxis(position.y, CommandType.lookDown, CommandType.lookUp, 1f))
}

fun updateGameInput2(config: GameInputConfig, events: ProfileStates<CommandType>, arguments: InputArguments): HaftCommands<CommandType> {
  val commands = gatherProfileCommands3(events)
  val mouseMovementCommands = if (config.mouseInput)
    applyMouseMovement(arguments)
  else
    listOf()
//    handleKeystrokeCommands(commands, keyStrokeCommands)
  return commands.plus(mouseMovementCommands)
}

fun getInputArguments(input: PlatformInput, state: InputDeviceState, players: List<Int>): InputArguments {
  val gamepads = input.getGamepads().map { it.id }
  val waitingDevices = getWaitingDevices(gamepadAssignments, gamepads)
  val deviceHandlers = createDeviceHandlers(input, gamepadAssignments)
      .plus(waitingDevices.map {
        { trigger: Int -> input.GamepadInputSource(it, trigger) }
      })
  return InputArguments(
      deviceHandlers,
      waitingDevices,
      state,
      players,
      input.getMousePosition()
  )
}

private var previousMousePosition = Vector2i()

//data class LabInputState(
//    val commands: List<HaftCommand<LabCommandType>>,
//    val mousePosition: Vector2i,
//    val mouseOffset: Vector2i
//)

fun updateInputDeviceState(input: PlatformInput, scenes: List<GameScene>, clientState: ClientState): InputDeviceState {
  input.update()
  val properties = getInputArguments(state.device, scenes.map { it.player })
  val mainEvents = updateGameInput1(properties, clientState)
//    rendering.updateGameInput(properties, rendering.playerInputProfiles)

  val waitingEvents = checkForNewGamepads1(properties)

  val mousePosition = input.getMousePosition()
  return InputDeviceState(
      profileStates = mainEvents,
      mousePosition = mousePosition
  )
}

fun getCommandState(platformInput: PlatformInput, inputState: InputDeviceState): CommandState {
  platformInput.update()
  val allCommands = updateGameInput2(mainEvents, properties)
      .plus(checkForNewGamepads2(waitingEvents, properties.players.size))

  val mousePosition = platformInput.getMousePosition()
  val input = CommandState(
      commands = allCommands,
      mousePosition = mousePosition,
      mouseOffset = mousePosition - previousMousePosition
  )
  previousMousePosition = mousePosition
  return input
}

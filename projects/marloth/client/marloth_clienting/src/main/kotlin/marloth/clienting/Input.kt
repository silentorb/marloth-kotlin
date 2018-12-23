package marloth.clienting

import haft.*
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.currentView
import mythic.bloom.ButtonState
import mythic.platforming.InputEvent
import mythic.platforming.PlatformInput
import mythic.spatial.Vector2
import mythic.spatial.toVector2i
import org.joml.Vector2i
import org.joml.minus

val gamepadSlotStart = 2

typealias UserCommand = HaftCommand<CommandType>

typealias UserCommands = List<UserCommand>

data class GeneralInputDeviceState(
    val profileStates: List<InputEvent>,
    val mousePosition: Vector2
)

data class GeneralCommandState<CT>(
    val commands: List<HaftCommand<CT>>,
    val mousePosition: Vector2,
    val mouseOffset: Vector2
)

typealias CommandState = GeneralCommandState<CommandType>

typealias InputDeviceState = GeneralInputDeviceState

data class GameInputConfig(
    var mouseInput: Boolean = true
)

data class InputState(
    val device: InputDeviceState,
    val config: GameInputConfig,
    val gameProfiles: List<PlayerInputProfile>,
    val menuProfiles: List<PlayerInputProfile>,
    val deviceMap: DeviceMap
)

fun newInputDeviceState() =
    InputDeviceState(
        profileStates = mapOf(),
        mousePosition = Vector2()
    )

//fun initialGameInputState(): ProfileStates<CommandType> = mapOf()
////        , (1..maxPlayerCount).map { null }
////)

//fun updateGamepadSlots(input: PlatformInput, previousMap: GamepadSlots): GamepadSlots =
//    updateGamepadSlots(input.getGamepads().map { it.id }, previousMap)
//
//fun selectGamepadHandler(GamepadInputSource: MultiDeviceScalarInputSource, gamepad: Int?, isActive: Boolean) =
//    if (gamepad != null && isActive)
//      { trigger: Int -> GamepadInputSource(gamepad, trigger) }
//    else
//      disconnectedScalarInputSource

//fun getWaitingDevices(gamepadAssignments: MutableMap<Int, Int>, gamepads: List<GamepadDeviceId>) =
//    gamepads.filter { d -> !gamepadAssignments.any { it.key == d } }
//
//fun createDeviceHandlers(input: PlatformInput, gamepadAssignments: MutableMap<Int, Int>): List<ScalarInputSource> {
//  return listOf(
//      input.KeyboardInputSource,
//      input.MouseInputSource
//  )
//      .plus(gamepadAssignments.map {
//        { trigger: Int -> input.GamepadInputSource(it.key, trigger) }
//      })
//      .plus((1..(maxPlayerCount - gamepadAssignments.size)).map {
//        disconnectedScalarInputSource
//      })
//}

data class PlayerInputProfile(
    val player: Int,
    val gameBindings: Bindings<CommandType>,
    val menuBindings: Bindings<CommandType>
)

//data class AvailableInputProfiles(
//    val playerInputProfiles: List<PlayerInputProfile>
//)

//fun selectActiveInputProfiles(playerInputProfiles: List<PlayerInputProfile>, players: List<Int>) =
//    playerInputProfiles.filter { players.contains(it.player) }
//        .map { it.gameBindings }

//data class InputArguments(
//    val deviceHandlers: List<ScalarInputSource>,
//    val waitingDevices: List<GamepadDeviceId>,
//    val previousState: InputDeviceState,
//    val players: List<Int>
//)

//val playerInputProfiles = defaultGameInputProfiles()
//val menuInputProfiles = defaultMenuInputProfiles()

// TODO: Make gamepadAssignments a part of InputState
val gamepadAssignments: MutableMap<Int, Int> = mutableMapOf()

//class ClientInput(val input: PlatformInput, val config: GameInputConfig) {

//fun checkForNewGamepads1(arguments: InputArguments): ProfileStates<CommandType> {
//  val (deviceHandlers, waitingDevices, previousState, players) = arguments
//  val profiles = createWaitingGamepadProfiles(waitingDevices.size, gamepadAssignments.size)
//  return gatherProfileCommands2(profiles, previousState.profileStates, deviceHandlers)
//}
//
//fun checkForNewGamepads2(events: ProfileStates<CommandType>, playerCount: Int): HaftCommands<CommandType> {
//  val commands = gatherProfileCommands3(events)
//  if (commands.size > 0)
//    println(commands.size)
//  var playerCounter = playerCount
//  val keystrokes = filterKeystrokeCommands(commands, listOf(CommandType.activateDevice, CommandType.joinGame))
//  for (command in keystrokes) {
//    gamepadAssignments[command.target - 10] = playerCounter++
//  }
//  return commands
//}
//
//fun updateGameInput1(arguments: InputArguments, inputProfiles: List<PlayerInputProfile>): ProfileStates<CommandType> {
//  val profiles = selectActiveInputProfiles(inputProfiles, arguments.players)
//  return gatherProfileCommands2(profiles, arguments.previousState.profileStates, arguments.deviceHandlers)
//}

fun applyMouseAxis(value: Float, firstCommandType: CommandType, secondCommandType: CommandType, scale: Float) =
    if (value > 0)
      listOf(HaftCommand(firstCommandType, 1, value * scale))
    else if (value < 0)
      listOf(HaftCommand(secondCommandType, 1, -value * scale))
    else
      listOf()

fun applyMouseMovement(mouseOffset: Vector2): HaftCommands<CommandType> {
  return listOf<HaftCommand<CommandType>>()
      .plus(applyMouseAxis(mouseOffset.x, CommandType.lookRight, CommandType.lookLeft, 1f))
      .plus(applyMouseAxis(mouseOffset.y, CommandType.lookDown, CommandType.lookUp, 1f))
}

//fun getInputArguments(input: PlatformInput, state: InputDeviceState, players: List<Int>): InputArguments {
//  val gamepads = input.getGamepads().map { it.id }
//  val waitingDevices = getWaitingDevices(gamepadAssignments, gamepads)
//  val deviceHandlers = createDeviceHandlers(input, gamepadAssignments)
//      .plus(waitingDevices.map {
//        { trigger: Int -> input.GamepadInputSource(it, trigger) }
//      })
//  return InputArguments(
//      deviceHandlers,
//      waitingDevices,
//      state,
//      players
//  )
//}

private var previousMousePosition = Vector2()

//data class LabInputState(
//    val commands: List<HaftCommand<LabCommandType>>,
//    val mousePosition: Vector2i,
//    val mouseOffset: Vector2i
//)

fun selectProfiles(clientState: ClientState): List<PlayerInputProfile> =
    if (currentView(clientState.bloomState.bag) != ViewId.none)
      clientState.input.menuProfiles
    else
      clientState.input.gameProfiles

fun updateInputDeviceState(input: PlatformInput, players: List<Int>, inputState: InputState, profiles: List<PlayerInputProfile>): InputDeviceState {
  input.update()
  val events = input.getEvents()
  val mappedEvents = events.mapNotNull { event ->
    val device = inputState.deviceMap[event.device]
    if (device != null)
      event.copy(
          device = device
      )
    else
      null
  }

//  val properties = getInputArguments(input, inputState.device, players)
//  val mainEvents = updateGameInput1(properties, profiles)
//
//  val waitingEvents = checkForNewGamepads1(properties)

  val mousePosition = input.getMousePosition()
  return InputDeviceState(
      profileStates = mainEvents.plus(waitingEvents),
      mousePosition = mousePosition
  )
}

fun getCommandState(deviceState: InputDeviceState, config: GameInputConfig, playerCount: Int): CommandState {
  val commands = gatherProfileCommands3(deviceState.profileStates)
  val mousePosition = deviceState.mousePosition
  val mouseOffset = mousePosition - previousMousePosition
  val mouseMovementCommands = if (config.mouseInput)
    applyMouseMovement(mouseOffset)
  else
    listOf()
//    handleKeystrokeCommands(commands, keyStrokeCommands)
  val allCommands = commands
      .plus(mouseMovementCommands)
      .plus(checkForNewGamepads2(deviceState.profileStates, playerCount))

  val input = CommandState(
      commands = allCommands,
      mousePosition = mousePosition,
      mouseOffset = mouseOffset
  )
  previousMousePosition = mousePosition
  return input
}

fun newBloomInputState(input: PlatformInput) =
    mythic.bloom.InputState(
        mousePosition = input.getMousePosition().toVector2i(),
        mouseButtons = listOf(
            if (input.MouseInputSource(0) == 1f)
              ButtonState.down
            else
              ButtonState.up
        ),
        events = listOf()
    )

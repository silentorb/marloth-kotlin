package marloth.clienting.input

import DeviceMap
import PlayerDevice
import haft.*
import marloth.clienting.ClientState
import marloth.clienting.getBinding
import marloth.clienting.gui.ViewId
import mythic.bloom.BloomId
import mythic.platforming.InputEvent
import simulation.input.CommandType

typealias UserCommand = HaftCommand<GuiCommandType>

typealias UserCommands = List<UserCommand>

data class GameInputConfig(
    var mouseInput: Boolean = true
)

enum class BindingContext {
  game,
  menu
}

data class InputProfile<T>(
    val bindings: Bindings<T>
)

data class InputState(
    val deviceStates: List<InputDeviceState>,
    val config: GameInputConfig,
    val guiInputProfiles: Map<BloomId, InputProfile<GuiCommandType>>,
    val gameInputProfiles: Map<BloomId, InputProfile<CommandType>>,
    val playerProfiles: Map<BloomId, BloomId>,
    val deviceMap: DeviceMap
)

fun newInputState(config: GameInputConfig) =
    InputState(
        deviceStates = listOf(newInputDeviceState()),
        config = config,
        guiInputProfiles = mapOf(1 to defaultInputProfile()),
        gameInputProfiles = mapOf(1 to defaultGameInputProfile()),
        playerProfiles = mapOf(
            1 to 1,
            2 to 1,
            3 to 1,
            4 to 1
        ),
        deviceMap = mapOf(
            0 to PlayerDevice(1, DeviceIndex.keyboard),
            1 to PlayerDevice(1, DeviceIndex.mouse)
        )
    )

fun bindingContext(clientState: ClientState): BindingContext =
    if (clientState.view != ViewId.none)
      BindingContext.menu
    else
      BindingContext.game

fun gamePadJoinCommands(events: List<InputEvent>, deviceMap: DeviceMap): List<Int> {
  val currentDevices = deviceMap.keys
  return events
      .filter { !currentDevices.contains(it.device) }
      .map { it.device }
      .distinct()
}

fun currentGamepadPlayers(deviceMap: DeviceMap) =
    deviceMap
        .filter { it.value.device == DeviceIndex.gamepad }
        .map { it.value.player }
        .distinct()

fun newGamepadDeviceEntry(device:Int, player: Int): Pair<Int, PlayerDevice> {
  println("gamepad $device $player")
  return Pair(device, PlayerDevice(
      player = player,
      device = DeviceIndex.gamepad
  ))
}

fun updateDeviceMapWithNewPlayers(deviceStates: List<InputDeviceState>): (DeviceMap) -> DeviceMap = { deviceMap ->
  val gamepadJoinCommands = gamePadJoinCommands(deviceStates.last().events, deviceMap)
  if (gamepadJoinCommands.none()) {
    deviceMap
  } else {
    val playersWithoutGamepads = (1..4).minus(currentGamepadPlayers(deviceMap))
    val newEntries = gamepadJoinCommands.zip(playersWithoutGamepads, ::newGamepadDeviceEntry)
    deviceMap.plus(newEntries)
  }
}

fun gatherInputCommands(inputState: InputState, bindingContext: BindingContext): HaftCommands<GuiCommandType> {
  val getBinding = getBinding(inputState, inputState.guiInputProfiles)
  val strokes = clientCommandStrokes[bindingContext]!!
  val deviceStates = inputState.deviceStates
  return mapEventsToCommands(deviceStates, strokes, getBinding)
}

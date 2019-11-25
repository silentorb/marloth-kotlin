package marloth.clienting.input

import DeviceMap
import PlayerDevice
import haft.*
import marloth.clienting.ClientState
import marloth.clienting.getBinding
import marloth.clienting.gui.ViewId
import mythic.bloom.BloomId
import mythic.ent.Id
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
        guiInputProfiles = mapOf(1L to defaultInputProfile()),
        gameInputProfiles = mapOf(1L to defaultGameInputProfile()),
        playerProfiles = mapOf(
            1L to 1L,
            2L to 1L,
            3L to 1L,
            4L to 1L
        ),
        deviceMap = mapOf(
            0 to PlayerDevice(1, DeviceIndex.keyboard),
            1 to PlayerDevice(1, DeviceIndex.mouse)
        )
    )

fun bindingContext(clientState: ClientState, player: Id): BindingContext =
    if ((clientState.playerViews[player] ?: ViewId.none) != ViewId.none)
      BindingContext.menu
    else
      BindingContext.game

fun joiningGamepads(events: List<InputEvent>, deviceMap: DeviceMap): List<Int> {
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

fun newGamepadDeviceEntry(device:Int, player: Id): Pair<Int, PlayerDevice> {
  println("gamepad $device $player")
  return Pair(device, PlayerDevice(
      player = player,
      device = DeviceIndex.gamepad
  ))
}

fun gatherInputCommands(inputState: InputState, bindingContext: BindingContext): HaftCommands<GuiCommandType> {
  val getBinding = getBinding(inputState, inputState.guiInputProfiles)
  val strokes = clientCommandStrokes[bindingContext]!!
  val deviceStates = inputState.deviceStates
  return mapEventsToCommands(deviceStates, strokes, getBinding)
}

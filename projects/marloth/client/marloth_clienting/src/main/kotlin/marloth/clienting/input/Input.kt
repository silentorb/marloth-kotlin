package marloth.clienting.input

import DeviceMap
import PlayerDevice
import haft.*
import marloth.clienting.ClientState
import marloth.clienting.gui.ViewId
import mythic.bloom.BloomId
import mythic.bloom.ButtonState
import mythic.platforming.PlatformInput
import mythic.platforming.mouseDeviceIndex
import mythic.spatial.Vector2
import mythic.spatial.toVector2i
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

fun updateDeviceMap(deviceStates: List<InputDeviceState>, input: InputState): DeviceMap {
  val currentDevices = input.deviceMap.keys
  val gamePadSelectCommands = deviceStates.last().events.filter { !currentDevices.contains(it.device) }
      .distinctBy { it.device }
  if (gamePadSelectCommands.none())
    return input.deviceMap

  val currentGamepadPlayers = input.deviceMap
      .filter { it.value.device == DeviceIndex.gamepad }
      .map { it.value.player }.distinct()

  val playersWithoutGamepads = (1..4).minus(currentGamepadPlayers)

  return input.deviceMap.plus(gamePadSelectCommands.mapIndexed { i, it ->
    println("gamepad " + it.device + " " + playersWithoutGamepads[i])
    Pair(it.device, PlayerDevice(
        player = playersWithoutGamepads[i],
        device = DeviceIndex.gamepad
    ))
  })
}

fun updateInputState(deviceStates: List<InputDeviceState>, input: InputState): InputState {
  return input.copy(
      deviceStates = deviceStates,
      deviceMap = updateDeviceMap(deviceStates, input)
  )
}

package marloth.clienting.input

import DeviceMap
import PlayerDevice
import haft.*
import marloth.clienting.ClientState
import marloth.clienting.PlayerViews
import marloth.clienting.gui.ViewId
import mythic.bloom.BloomId
import mythic.ent.Id
import mythic.platforming.InputEvent

typealias UserCommand = HaftCommand

typealias UserCommands = List<UserCommand>

data class GameInputConfig(
    var mouseInput: Boolean = true
)

enum class InputContext {
  game,
  menu
}

data class InputProfile(
    val bindings: Map<InputContext, Bindings>
)

data class InputState(
    val deviceStates: List<InputDeviceState>,
    val config: GameInputConfig,
    val inputProfiles: Map<Id, InputProfile>,
    val playerProfiles: Map<BloomId, BloomId>,
    val deviceMap: DeviceMap
)

fun newInputState(config: GameInputConfig) =
    InputState(
        deviceStates = listOf(newInputDeviceState()),
        config = config,
        inputProfiles = mapOf(
            1L to InputProfile(
                bindings = mapOf(
                    InputContext.game to defaultGameInputBindings(),
                    InputContext.menu to defaultMenuInputProfile()
                )
            )
        ),
        playerProfiles = mapOf(),
        deviceMap = mapOf(
            0 to PlayerDevice(1, DeviceIndex.keyboard),
            1 to PlayerDevice(1, DeviceIndex.mouse)
        )
    )

fun bindingContext(playerViews: PlayerViews, player: Id): InputContext =
    if ((playerViews[player] ?: ViewId.none) != ViewId.none)
      InputContext.menu
    else
      InputContext.game

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

fun newGamepadDeviceEntry(device: Int, player: Id): Pair<Int, PlayerDevice> {
  println("gamepad $device $player")
  return Pair(device, PlayerDevice(
      player = player,
      device = DeviceIndex.gamepad
  ))
}

fun getInputProfile(inputState: InputState, player: Id): InputProfile? {
  val playerProfile = inputState.playerProfiles[player]
  return inputState.inputProfiles[playerProfile]
}

fun getBinding(inputState: InputState, playerViews: PlayerViews): BindingSource = { event ->
  val playerDevice = inputState.deviceMap[event.device]
  if (playerDevice != null) {
    val player = playerDevice.player
    val profile = getInputProfile(inputState, player)
    if (profile != null) {
      val inputContext = bindingContext(playerViews, player)
      val binding = profile.bindings
          .getValue(inputContext)
          .firstOrNull { it.device == playerDevice.device && it.trigger == event.index }
      if (binding != null)
        Pair(binding, player)
      else
        null
    } else
      null
  } else
    null
}

fun gatherInputCommands(inputState: InputState, playerViews: PlayerViews): HaftCommands {
  val getBinding = getBinding(inputState, playerViews)
  val deviceStates = inputState.deviceStates
  return mapEventsToCommands(deviceStates, clientCommandStrokes, getBinding)
}

package marloth.clienting.input

import silentorb.mythic.haft.*
import marloth.clienting.PlayerViews
import marloth.clienting.menus.ViewId
import silentorb.mythic.bloom.input.DeviceMap
import silentorb.mythic.ent.Id
import silentorb.mythic.platforming.InputEvent

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

const val defaultInputProfile: Long = 1L
typealias InputProfileId = Long

data class InputState(
    val deviceStates: List<InputDeviceState>,
    val config: GameInputConfig,
    val inputProfiles: Map<InputProfileId, InputProfile>,
    val playerProfiles: Map<Id, InputProfileId>,
    val deviceMap: DeviceMap,
    val devicePlayers: Map<Int, Id>
)

fun newInputState(config: GameInputConfig) =
    InputState(
        deviceStates = listOf(newInputDeviceState()),
        config = config,
        inputProfiles = mapOf(
            defaultInputProfile to InputProfile(
                bindings = mapOf(
                    InputContext.game to defaultGameInputBindings(),
                    InputContext.menu to defaultMenuInputProfile()
                )
            )
        ),
        playerProfiles = mapOf(),
        deviceMap = mapOf(),
        devicePlayers = mapOf()
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

//fun currentGamepadPlayers(devicePlayerMap: DevicePlayerMap) =
//    devicePlayerMap
//        .filter { it.value.device == DeviceIndex.gamepad }
//        .map { it.value.player }
//        .distinct()

//fun newGamepadDeviceEntry(device: Int, player: Id): Pair<Int, PlayerDevice> {
//  println("gamepad $device $player")
//  return Pair(device, PlayerDevice(
//      player = player,
//      device = DeviceIndex.gamepad
//  ))
//}

fun getInputProfile(inputState: InputState, player: Id): InputProfile? {
  val playerProfile = inputState.playerProfiles[player]
  return inputState.inputProfiles[playerProfile]
}

fun isStroke(context: InputContext, type: Any): Boolean =
    clientCommandStrokes[context]!!.contains(type)

fun getBinding(inputState: InputState, playerViews: PlayerViews): BindingSource = { event ->
  val player = inputState.devicePlayers[event.device]
  val device = inputState.deviceMap[event.device]
  if (player != null && device != null) {
       val profile = getInputProfile(inputState, player)
    if (profile != null) {
      val inputContext = bindingContext(playerViews, player)
      val binding = profile.bindings
          .getValue(inputContext)
          .firstOrNull { it.device == device && it.trigger == event.index }
      if (binding != null)
        Triple(binding, player, isStroke(inputContext, binding.command))
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
  return mapEventsToCommands(deviceStates, getBinding)
}

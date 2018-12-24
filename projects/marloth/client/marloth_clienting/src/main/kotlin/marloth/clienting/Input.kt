package marloth.clienting

import haft.*
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.currentView
import mythic.bloom.ButtonState
import mythic.ent.Id
import mythic.platforming.InputEvent
import mythic.platforming.PlatformInput
import mythic.spatial.Vector2
import mythic.spatial.toVector2i

val gamepadSlotStart = 2

typealias UserCommand = HaftCommand<CommandType>

typealias UserCommands = List<UserCommand>

data class GeneralInputDeviceState(
    val events: List<InputEvent>,
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

enum class BindingMode {
  game,
  menu
}

typealias GroupedBindings = Map<BindingMode, Bindings<CommandType>>

data class InputProfile(
    val bindings: GroupedBindings
)

data class PlayerInputProfile(
    val player: Int,
    val profile: Id
)

data class PlayerDevice(
    val player: Int,
    val device: DeviceIndex
)

typealias DeviceMap = Map<Int, PlayerDevice>

data class InputState(
    val deviceStates: List<InputDeviceState>,
    val config: GameInputConfig,
    val profiles: Map<Id, InputProfile>,
    val playerProfiles: List<PlayerInputProfile>,
    val deviceMap: DeviceMap
)

fun newInputDeviceState() =
    InputDeviceState(
        events = listOf(),
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


// TODO: Make gamepadAssignments a part of InputState
val gamepadAssignments: MutableMap<Int, Int> = mutableMapOf()

fun bindingMode(clientState: ClientState): BindingMode =
    if (currentView(clientState.bloomState.bag) != ViewId.none)
      BindingMode.menu
    else
      BindingMode.game

fun updateInputDeviceState(input: PlatformInput): InputDeviceState {
  input.update()
  return InputDeviceState(
      events = input.getEvents(),
      mousePosition = input.getMousePosition()
  )
}

fun mapEventsToCommands(deviceStates: List<InputDeviceState>, state: InputState, bindingMode: BindingMode): HaftCommands<CommandType> =
    deviceStates.last().events.mapNotNull { event ->
      val playerDevice = state.deviceMap[event.device]
      if (playerDevice != null) {
        val playerProfile = state.playerProfiles[playerDevice.player]
        val profile = state.profiles[playerProfile.profile]!!
        val binding = profile.bindings[bindingMode]!!.firstOrNull { it.device == playerDevice.device && it.trigger == event.index }
        if (binding != null) {
          HaftCommand(
              type = binding.command,
              target = playerProfile.player,
              value = event.value
          )
        } else
          null
      } else
        null
    }

fun mouseMovementEvents(deviceStates: List<InputDeviceState>): HaftCommands<CommandType> {
  throw Error("Not implemented")
//  val mousePosition = deviceStates[1].mousePosition
//  val mouseOffset = mousePosition - deviceStates[0].mousePosition
//  return if (config.mouseInput)
//    applyMouseMovement(mouseOffset)
//  else
//    listOf()
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

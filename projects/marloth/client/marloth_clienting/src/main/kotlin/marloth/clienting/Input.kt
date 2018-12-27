package marloth.clienting

import haft.*
import marloth.clienting.gui.ViewId
import mythic.bloom.ButtonState
import mythic.ent.Id
import mythic.platforming.InputEvent
import mythic.platforming.PlatformInput
import mythic.platforming.mouseDeviceIndex
import mythic.spatial.Vector2
import mythic.spatial.toVector2i

typealias UserCommand = HaftCommand<CommandType>

typealias UserCommands = List<UserCommand>

data class GeneralCommandState<CT>(
    val commands: List<HaftCommand<CT>>,
    val mousePosition: Vector2,
    val mouseOffset: Vector2
)

data class GameInputConfig(
    var mouseInput: Boolean = true
)

enum class BindingContext {
  game,
  menu
}

typealias GroupedBindings = Map<BindingContext, Bindings<CommandType>>

data class InputProfile(
    val bindings: GroupedBindings
)

data class PlayerDevice(
    val player: Id,
    val device: DeviceIndex
)

typealias DeviceMap = Map<Int, PlayerDevice>

data class InputState(
    val deviceStates: List<InputDeviceState>,
    val config: GameInputConfig,
    val profiles: Map<Id, InputProfile>,
    val playerProfiles: Map<Id, Id>,
    val deviceMap: DeviceMap
)

fun newInputDeviceState() =
    InputDeviceState(
        events = listOf(),
        mousePosition = Vector2()
    )

fun bindingContext(clientState: ClientState): BindingContext =
    if (clientState.view != ViewId.none)
      BindingContext.menu
    else
      BindingContext.game

fun updateInputDeviceState(input: PlatformInput): InputDeviceState {
  input.update()
  return InputDeviceState(
      events = input.getEvents(),
      mousePosition = input.getMousePosition()
  )
}

fun updateInputState(input: PlatformInput, inputState: InputState): List<InputDeviceState> {
  val newDeviceState = updateInputDeviceState(input)
  return listOf(inputState.deviceStates.last(), newDeviceState)
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

fun newBloomInputState(deviceState: InputDeviceState) =
    mythic.bloom.InputState(
        mousePosition = deviceState.mousePosition.toVector2i(),
        mouseButtons = listOf(
            if (deviceState.events.any { it.device == mouseDeviceIndex && it.index == 0 })
              ButtonState.down
            else
              ButtonState.up
        ),
        events = listOf()
    )

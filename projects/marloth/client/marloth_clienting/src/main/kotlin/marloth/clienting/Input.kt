package marloth.clienting

import haft.*
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.currentView
import mythic.bloom.ButtonState
import mythic.ent.Id
import mythic.platforming.PlatformInput
import mythic.spatial.Vector2
import mythic.spatial.toVector2i

val gamepadSlotStart = 2

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

data class PlayerInputProfile(
    val player: Int,
    val profile: Id
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
    if (currentView(clientState.bloomState.bag) != ViewId.none)
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

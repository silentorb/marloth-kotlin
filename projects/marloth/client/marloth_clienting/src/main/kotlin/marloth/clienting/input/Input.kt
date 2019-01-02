package marloth.clienting.input

import haft.*
import marloth.clienting.ClientState
import marloth.clienting.gui.ViewId
import mythic.bloom.BloomId
import mythic.bloom.ButtonState
import mythic.platforming.PlatformInput
import mythic.platforming.mouseDeviceIndex
import mythic.spatial.Vector2
import mythic.spatial.toVector2i

typealias UserCommand = HaftCommand<GuiCommandType>

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

//typealias GroupedBindings<T> = Map<BindingContext, Bindings<T>>

data class InputProfile<T>(
    val bindings: Bindings<T>
)

data class PlayerDevice(
    val player: BloomId,
    val device: DeviceIndex
)

typealias DeviceMap = Map<Int, PlayerDevice>

data class InputState(
    val deviceStates: List<InputDeviceState>,
    val config: GameInputConfig,
    val guiInputProfiles: Map<BloomId, InputProfile<GuiCommandType>>,
    val gameInputProfiles: Map<BloomId, InputProfile<simulation.CommandType>>,
    val playerProfiles: Map<BloomId, BloomId>,
    val deviceMap: DeviceMap
)

fun newInputDeviceState() =
    InputDeviceState(
        events = listOf(),
        mousePosition = Vector2()
    )

fun newInputState(config: GameInputConfig) =
    InputState(
        deviceStates = listOf(newInputDeviceState()),
        config = config,
        guiInputProfiles = mapOf(1 to defaultInputProfile()),
        gameInputProfiles = mapOf(1 to defaultGameInputProfile()),
        playerProfiles = mapOf(
            1 to 1
        ),
        deviceMap = mapOf(
            0 to PlayerDevice(1, DeviceIndex.keyboard),
            1 to PlayerDevice(1, DeviceIndex.keyboard),
            2 to PlayerDevice(1, DeviceIndex.gamepad),
            3 to PlayerDevice(1, DeviceIndex.gamepad),
            4 to PlayerDevice(1, DeviceIndex.gamepad),
            5 to PlayerDevice(1, DeviceIndex.gamepad)
        )
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

fun mouseMovementEvents(deviceStates: List<InputDeviceState>): HaftCommands<GuiCommandType> {
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

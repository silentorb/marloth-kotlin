package marloth.clienting

import commanding.CommandType
import haft.ScalarInputSource
import haft.disconnectedScalarInputSource
import mythic.platforming.PlatformInput
import haft.*
import org.lwjgl.glfw.GLFW.*

val maxGamepadCount = 4
val gamepadSlotStart = 2

fun initialGameInputState() =
    HaftInputState<CommandType>(mapOf(), (1..maxGamepadCount).map { null })

fun updateGamepadSlots(input: PlatformInput, previousMap: GamepadSlots): GamepadSlots =
    updateGamepadSlots(input.getGamepads().map { it.id }, previousMap)

fun selectGamepadHandler(GamepadInputSource: MultiDeviceScalarInputSource, gamepad: Int?, isActive: Boolean) =
    if (gamepad != null && isActive)
      { trigger: Int -> GamepadInputSource(gamepad, trigger) }
    else
      disconnectedScalarInputSource

fun createDeviceHandlers(input: PlatformInput, gamepadSlots: GamepadSlots, players: List<Boolean>): List<ScalarInputSource> {
  return listOf(
      input.KeyboardInputSource,
      disconnectedScalarInputSource
  )
      .plus(gamepadSlots.mapIndexed { i, it ->
        selectGamepadHandler(input.GamepadInputSource, it, players[i])
      })
      .plus(gamepadSlots.mapIndexed { i, it ->
        selectGamepadHandler(input.GamepadInputSource, it, !players[i])
//        disconnectedScalarInputSource
      })
}

fun defaultKeyboardProfile() = mapOf(
    GLFW_KEY_W to CommandType.moveUp,
    GLFW_KEY_A to CommandType.moveLeft,
    GLFW_KEY_D to CommandType.moveRight,
    GLFW_KEY_S to CommandType.moveDown,

    GLFW_KEY_TAB to CommandType.switchView,

    GLFW_KEY_ESCAPE to CommandType.menuBack
)

fun waitingGamepadBinding() = mapOf(
    GAMEPAD_BUTTON_START to CommandType.activateDevice
)

fun defaultGamepadBindings() = mapOf(
    GAMEPAD_AXIS_LEFT_UP to CommandType.moveUp,
    GAMEPAD_AXIS_LEFT_DOWN to CommandType.moveDown,
    GAMEPAD_AXIS_LEFT_LEFT to CommandType.moveLeft,
    GAMEPAD_AXIS_LEFT_RIGHT to CommandType.moveRight,

    GAMEPAD_BUTTON_BACK to CommandType.switchView,
    GAMEPAD_BUTTON_START to CommandType.menuBack
)

data class PlayerInputProfile(
    val player: Int,
    val gameBindings: Bindings<CommandType>,
    val menuBindings: Bindings<CommandType>
)

data class AvailableInputProfiles(
    val playerInputProfiles: List<PlayerInputProfile>
)

fun createPrimaryInputProfile(player: Int, gamepad: Int) =
    PlayerInputProfile(player,
        createBindings(0, player, defaultKeyboardProfile())
            .plus(createBindings(gamepad, player, defaultGamepadBindings())),
        listOf()
    )

fun createSecondaryInputProfile(player: Int, device: Int) =
    PlayerInputProfile(player,
        createBindings(device, player, defaultGamepadBindings()),
        listOf()
    )

fun createDefaultInputProfiles() = listOf(
    createPrimaryInputProfile(1, 2),
    createSecondaryInputProfile(2, 3),
    createSecondaryInputProfile(3, 4),
    createSecondaryInputProfile(4, 5)
)

fun createWaitingGamepadProfiles() =
    (0 until maxGamepadCount).map {
      createStrokeBindings(gamepadSlotStart + maxGamepadCount + it, 0, waitingGamepadBinding())
    }

fun selectActiveInputProfiles(playerInputProfiles: List<PlayerInputProfile>,
                              bindings: List<List<Binding<CommandType>>>,
                              players: List<Int>) =
    playerInputProfiles.filter { players.contains(it.player) }
        .map { it.gameBindings }
        .plus(bindings
//            .filterIndexed { i, it->gamepadSlots[i] != null}
        )

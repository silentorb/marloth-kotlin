package marloth.clienting

import commanding.CommandType
import haft.ScalarInputSource
import haft.disconnectedScalarInputSource
import mythic.platforming.PlatformInput
import haft.*
import org.lwjgl.glfw.GLFW.*

val gamepadSlotStart = 2

fun initialGameInputState() =
    HaftInputState<CommandType>(mapOf()
//        , (1..maxPlayerCount).map { null }
    )

fun updateGamepadSlots(input: PlatformInput, previousMap: GamepadSlots): GamepadSlots =
    updateGamepadSlots(input.getGamepads().map { it.id }, previousMap)

fun selectGamepadHandler(GamepadInputSource: MultiDeviceScalarInputSource, gamepad: Int?, isActive: Boolean) =
    if (gamepad != null && isActive)
      { trigger: Int -> GamepadInputSource(gamepad, trigger) }
    else
      disconnectedScalarInputSource

fun getWaitingDevices(gamepadAssignments: MutableMap<Int, Int>, gamepads: List<GamepadDeviceId>) =
    gamepads.filter { d -> !gamepadAssignments.any { it.key == d } }

fun createDeviceHandlers(input: PlatformInput, gamepadAssignments: MutableMap<Int, Int>,
                         waitingDevices: List<Int>): List<ScalarInputSource> {
  return listOf(
      input.KeyboardInputSource,
      disconnectedScalarInputSource
  )
      .plus(gamepadAssignments.map {
        { trigger: Int -> input.GamepadInputSource(it.key, trigger) }
      })
      .plus((1..(maxPlayerCount - gamepadAssignments.size)).map {
        disconnectedScalarInputSource
      })
      .plus(waitingDevices
          .map {
            { trigger: Int -> input.GamepadInputSource(it, trigger) }
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

fun commonGamepadBindings() = mapOf(
    GAMEPAD_AXIS_LEFT_UP to CommandType.moveUp,
    GAMEPAD_AXIS_LEFT_DOWN to CommandType.moveDown,
    GAMEPAD_AXIS_LEFT_LEFT to CommandType.moveLeft,
    GAMEPAD_AXIS_LEFT_RIGHT to CommandType.moveRight,

    GAMEPAD_BUTTON_BACK to CommandType.switchView,
    GAMEPAD_BUTTON_START to CommandType.menuBack
)

fun birdsEyeGamepadBindings() = mapOf(
    GAMEPAD_AXIS_RIGHT_UP to CommandType.attackUp,
    GAMEPAD_AXIS_RIGHT_DOWN to CommandType.attackDown,
    GAMEPAD_AXIS_RIGHT_LEFT to CommandType.attackLeft,
    GAMEPAD_AXIS_RIGHT_RIGHT to CommandType.attackRight
    )

//fun firstPersonGamepadBindings()= mapOf(
//
//)

fun allGamepadBindings() =
    commonGamepadBindings()
        .plus(birdsEyeGamepadBindings())

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
            .plus(createBindings(gamepad, player, allGamepadBindings())),
        listOf()
    )

fun createSecondaryInputProfile(player: Int, device: Int) =
    PlayerInputProfile(player,
        createBindings(device, player, allGamepadBindings()),
        listOf()
    )

fun createDefaultInputProfiles() = listOf(
    createPrimaryInputProfile(1, 2),
    createSecondaryInputProfile(2, 3),
    createSecondaryInputProfile(3, 4),
    createSecondaryInputProfile(4, 5)
)

fun createWaitingGamepadProfiles(count: Int, assignedGamepadCount: Int) =
    (0 until count).map {
      val bindings = if (assignedGamepadCount == 0)
        mapOf(GAMEPAD_BUTTON_START to CommandType.activateDevice)
      else
        mapOf(GAMEPAD_BUTTON_START to CommandType.joinGame)

      createStrokeBindings(gamepadSlotStart + maxPlayerCount + it, it + 10, bindings)
    }

fun selectActiveInputProfiles(playerInputProfiles: List<PlayerInputProfile>,
                              bindings: List<List<Binding<CommandType>>>,
                              players: List<Int>) =
    playerInputProfiles.filter { players.contains(it.player) }
        .map { it.gameBindings }
        .plus(bindings
//            .filterIndexed { i, it->gamepadSlots[i] != null}
        )

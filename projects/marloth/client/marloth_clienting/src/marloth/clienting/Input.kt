package marloth.clienting

import commanding.CommandType
import haft.ScalarInputSource
import haft.disconnectedScalarInputSource
import mythic.platforming.PlatformInput
import haft.*
import org.lwjgl.glfw.GLFW.*

val gamepadSlotStart = 2

fun initialGameInputState(): ProfileStates<CommandType> = mapOf()
//        , (1..maxPlayerCount).map { null }
//)

fun updateGamepadSlots(input: PlatformInput, previousMap: GamepadSlots): GamepadSlots =
    updateGamepadSlots(input.getGamepads().map { it.id }, previousMap)

fun selectGamepadHandler(GamepadInputSource: MultiDeviceScalarInputSource, gamepad: Int?, isActive: Boolean) =
    if (gamepad != null && isActive)
      { trigger: Int -> GamepadInputSource(gamepad, trigger) }
    else
      disconnectedScalarInputSource

fun getWaitingDevices(gamepadAssignments: MutableMap<Int, Int>, gamepads: List<GamepadDeviceId>) =
    gamepads.filter { d -> !gamepadAssignments.any { it.key == d } }

fun createDeviceHandlers(input: PlatformInput, gamepadAssignments: MutableMap<Int, Int>): List<ScalarInputSource> {
  return listOf(
      input.KeyboardInputSource,
      disconnectedScalarInputSource // Mouse
  )
      .plus(gamepadAssignments.map {
        { trigger: Int -> input.GamepadInputSource(it.key, trigger) }
      })
      .plus((1..(maxPlayerCount - gamepadAssignments.size)).map {
        disconnectedScalarInputSource
      })
}

fun defaultKeyboardGameBindings() = mapOf(
    GLFW_KEY_W to CommandType.moveUp,
    GLFW_KEY_A to CommandType.moveLeft,
    GLFW_KEY_D to CommandType.moveRight,
    GLFW_KEY_S to CommandType.moveDown
)

fun defaultKeyboardStrokeBindings() = mapOf(
    GLFW_KEY_TAB to CommandType.switchView,
    GLFW_KEY_ESCAPE to CommandType.menu
)

fun defaultKeyboardMenuBindings() = mapOf(
    GLFW_KEY_UP to CommandType.moveUp,
    GLFW_KEY_LEFT to CommandType.moveLeft,
    GLFW_KEY_RIGHT to CommandType.moveRight,
    GLFW_KEY_DOWN to CommandType.moveDown,
    GLFW_KEY_TAB to CommandType.switchView,
    GLFW_KEY_ESCAPE to CommandType.menu,
    GLFW_KEY_ENTER to CommandType.menuSelect,
    GLFW_KEY_SPACE to CommandType.menuSelect
)

val commonGamepadBindings = mapOf(
    GAMEPAD_AXIS_LEFT_UP to CommandType.moveUp,
    GAMEPAD_AXIS_LEFT_DOWN to CommandType.moveDown,
    GAMEPAD_AXIS_LEFT_LEFT to CommandType.moveLeft,
    GAMEPAD_AXIS_LEFT_RIGHT to CommandType.moveRight
)

val defaultGamepadMenuBindings = mapOf(
    
    GAMEPAD_AXIS_LEFT_UP to CommandType.moveUp,
    GAMEPAD_AXIS_LEFT_DOWN to CommandType.moveDown,
    GAMEPAD_AXIS_LEFT_LEFT to CommandType.moveLeft,
    GAMEPAD_AXIS_LEFT_RIGHT to CommandType.moveRight,
    
    GAMEPAD_AXIS_RIGHT_UP to CommandType.moveUp,
    GAMEPAD_AXIS_RIGHT_DOWN to CommandType.moveDown,
    GAMEPAD_AXIS_RIGHT_LEFT to CommandType.moveLeft,
    GAMEPAD_AXIS_RIGHT_RIGHT to CommandType.moveRight,

    GAMEPAD_BUTTON_DPAD_UP to CommandType.moveUp,
    GAMEPAD_BUTTON_DPAD_DOWN to CommandType.moveDown,
    GAMEPAD_BUTTON_DPAD_LEFT to CommandType.moveLeft,
    GAMEPAD_BUTTON_DPAD_RIGHT to CommandType.moveRight,

    GAMEPAD_BUTTON_START to CommandType.menu,
    GAMEPAD_BUTTON_A to CommandType.menuSelect,
    GAMEPAD_BUTTON_B to CommandType.menuBack
)

//val birdsEyeGamepadBindings = mapOf(
//    GAMEPAD_AXIS_RIGHT_UP to CommandType.attackUp,
//    GAMEPAD_AXIS_RIGHT_DOWN to CommandType.attackDown,
//    GAMEPAD_AXIS_RIGHT_LEFT to CommandType.attackLeft,
//    GAMEPAD_AXIS_RIGHT_RIGHT to CommandType.attackRight
//)

val firstPersonGamepadBindings = mapOf(
    GAMEPAD_AXIS_RIGHT_UP to CommandType.lookUp,
    GAMEPAD_AXIS_RIGHT_DOWN to CommandType.lookDown,
    GAMEPAD_AXIS_RIGHT_LEFT to CommandType.lookLeft,
    GAMEPAD_AXIS_RIGHT_RIGHT to CommandType.lookRight,

    GAMEPAD_AXIS_TRIGGER_RIGHT to CommandType.attack
)

val allGamepadStrokeBindings = mapOf(
    GAMEPAD_BUTTON_BACK to CommandType.switchView,
    GAMEPAD_BUTTON_START to CommandType.menu
)

fun allGamepadBindings() =
    commonGamepadBindings
        .plus(firstPersonGamepadBindings)

data class PlayerInputProfile(
    val player: Int,
    val gameBindings: Bindings<CommandType>,
    val menuBindings: Bindings<CommandType>
)

data class AvailableInputProfiles(
    val playerInputProfiles: List<PlayerInputProfile>
)

fun createDefaultGamepadBindings(gamepad: Int, player: Int) =
    createBindings(gamepad, player, allGamepadBindings())
        .plus(createStrokeBindings(gamepad, player, allGamepadStrokeBindings))

fun primaryGameInputProfile(player: Int, gamepad: Int) =
    PlayerInputProfile(player,
        createBindings(0, player, defaultKeyboardGameBindings())
            .plus(createStrokeBindings(0, player, defaultKeyboardStrokeBindings()))
            .plus(createDefaultGamepadBindings(gamepad, player)),
        listOf()
    )

fun primaryMenuInputProfile(player: Int, gamepad: Int) =
    PlayerInputProfile(player,
        createStrokeBindings(0, player, defaultKeyboardMenuBindings())
            .plus(createStrokeBindings(gamepad, player, defaultGamepadMenuBindings)),
        listOf()
    )

fun createSecondaryInputProfile(player: Int, device: Int) =
    PlayerInputProfile(player,
        createDefaultGamepadBindings(device, player),
        listOf()
    )

fun defaultGameInputProfiles() = listOf(
    primaryGameInputProfile(1, 2),
    createSecondaryInputProfile(2, 3),
    createSecondaryInputProfile(3, 4),
    createSecondaryInputProfile(4, 5)
)

fun defaultMenuInputProfiles() = listOf(
    primaryMenuInputProfile(1, 2)
)

fun createWaitingGamepadProfiles(count: Int, assignedGamepadCount: Int) =
    (0 until count).map {
      val bindings = if (assignedGamepadCount == 0)
        mapOf(GAMEPAD_BUTTON_START to CommandType.activateDevice)
      else
        mapOf(GAMEPAD_BUTTON_START to CommandType.joinGame)

      createStrokeBindings(gamepadSlotStart + maxPlayerCount + it, it + 10, bindings)
    }

fun selectActiveInputProfiles(playerInputProfiles: List<PlayerInputProfile>, players: List<Int>) =
    playerInputProfiles.filter { players.contains(it.player) }
        .map { it.gameBindings }


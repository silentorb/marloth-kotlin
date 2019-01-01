package marloth.clienting

import haft.*
import org.lwjgl.glfw.GLFW

fun defaultKeyboardGameBindings() = mapOf(
    GLFW.GLFW_KEY_W to CommandType.moveUp,
    GLFW.GLFW_KEY_A to CommandType.moveLeft,
    GLFW.GLFW_KEY_D to CommandType.moveRight,
    GLFW.GLFW_KEY_S to CommandType.moveDown
)

fun defaultKeyboardStrokeBindings() = mapOf(
    GLFW.GLFW_KEY_TAB to CommandType.switchView,
    GLFW.GLFW_KEY_ESCAPE to CommandType.menu
)

fun defaultKeyboardMenuBindings() = mapOf(
    GLFW.GLFW_KEY_UP to CommandType.moveUp,
    GLFW.GLFW_KEY_LEFT to CommandType.moveLeft,
    GLFW.GLFW_KEY_RIGHT to CommandType.moveRight,
    GLFW.GLFW_KEY_DOWN to CommandType.moveDown,
    GLFW.GLFW_KEY_TAB to CommandType.switchView,
    GLFW.GLFW_KEY_ESCAPE to CommandType.menu,
    GLFW.GLFW_KEY_ENTER to CommandType.menuSelect,
    GLFW.GLFW_KEY_SPACE to CommandType.menuSelect
)

fun defaultMouseGameStrokeBindings() = mapOf(
    GLFW.GLFW_MOUSE_BUTTON_LEFT to CommandType.attack
)

fun commonGamepadBindings() = mapOf(
    GAMEPAD_AXIS_LEFT_UP to CommandType.moveUp,
    GAMEPAD_AXIS_LEFT_DOWN to CommandType.moveDown,
    GAMEPAD_AXIS_LEFT_LEFT to CommandType.moveLeft,
    GAMEPAD_AXIS_LEFT_RIGHT to CommandType.moveRight
)

fun defaultGamepadMenuBindings() = mapOf(

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
    GAMEPAD_BUTTON_DPAD_UP to CommandType.equipSlot0,
    GAMEPAD_BUTTON_DPAD_LEFT to CommandType.equipSlot1,
    GAMEPAD_BUTTON_DPAD_RIGHT to CommandType.equipSlot2,
    GAMEPAD_BUTTON_DPAD_DOWN to CommandType.equipSlot3,
    GAMEPAD_BUTTON_BACK to CommandType.switchView,
    GAMEPAD_BUTTON_START to CommandType.menu
)

fun allGamepadBindings() =
    commonGamepadBindings()
        .plus(firstPersonGamepadBindings)

fun createDefaultGamepadBindings() =
    createBindings(DeviceIndex.gamepad, allGamepadBindings())
        .plus(createBindings(DeviceIndex.gamepad, allGamepadStrokeBindings))

fun defaultGameInputProfile() =
    createBindings(DeviceIndex.keyboard, defaultKeyboardGameBindings())
        .plus(createBindings(DeviceIndex.keyboard, defaultKeyboardStrokeBindings()))
        .plus(createBindings(DeviceIndex.mouse, defaultMouseGameStrokeBindings()))
        .plus(createDefaultGamepadBindings())

fun defaultMenuInputProfile() =
    createBindings(DeviceIndex.keyboard, defaultKeyboardMenuBindings())
        .plus(createBindings(DeviceIndex.gamepad, defaultGamepadMenuBindings()))

fun defaultInputProfile(): InputProfile = InputProfile(
    bindings = mapOf(
        BindingContext.game to defaultGameInputProfile(),
        BindingContext.menu to defaultMenuInputProfile()
    )
)

//fun createWaitingGamepadProfiles(count: Int, assignedGamepadCount: Int) =
//    (0 until count).map {
//      val bindings = if (assignedGamepadCount == 0)
//        mapOf(GAMEPAD_BUTTON_START to CommandType.activateDevice)
//      else
//        mapOf(GAMEPAD_BUTTON_START to CommandType.joinGame)
//
//      createBindings(gamepadSlotStart + maxPlayerCount + it, bindings)
//    }

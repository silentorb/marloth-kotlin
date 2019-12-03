package marloth.clienting.input

import haft.*
import org.lwjgl.glfw.GLFW
import simulation.input.CommandType as CommandType

val gameGamepadStrokeBindings = mapOf(
    GAMEPAD_BUTTON_START to GuiCommandType.menu,

    GAMEPAD_BUTTON_A to CommandType.ability,
    GAMEPAD_BUTTON_DPAD_UP to CommandType.equipSlot0,
    GAMEPAD_BUTTON_DPAD_LEFT to CommandType.equipSlot1,
    GAMEPAD_BUTTON_DPAD_RIGHT to CommandType.equipSlot2,
    GAMEPAD_BUTTON_DPAD_DOWN to CommandType.equipSlot3,

    GAMEPAD_AXIS_LEFT_UP to CommandType.moveUp,
    GAMEPAD_AXIS_LEFT_DOWN to CommandType.moveDown,
    GAMEPAD_AXIS_LEFT_LEFT to CommandType.moveLeft,
    GAMEPAD_AXIS_LEFT_RIGHT to CommandType.moveRight,

    GAMEPAD_AXIS_RIGHT_UP to CommandType.lookUp,
    GAMEPAD_AXIS_RIGHT_DOWN to CommandType.lookDown,
    GAMEPAD_AXIS_RIGHT_LEFT to CommandType.lookLeft,
    GAMEPAD_AXIS_RIGHT_RIGHT to CommandType.lookRight,

    GAMEPAD_BUTTON_A to CommandType.interactPrimary
)

fun defaultMouseGameStrokeBindings() = mapOf(
    GLFW.GLFW_MOUSE_BUTTON_LEFT to CommandType.ability
)

fun defaultKeyboardGameBindings() = mapOf(
    GLFW.GLFW_KEY_W to CommandType.moveUp,
    GLFW.GLFW_KEY_A to CommandType.moveLeft,
    GLFW.GLFW_KEY_D to CommandType.moveRight,
    GLFW.GLFW_KEY_S to CommandType.moveDown,
//    GLFW.GLFW_KEY_TAB to CommandType.switchView,
    GLFW.GLFW_KEY_ENTER to CommandType.interactPrimary
)

fun defaultGameInputBindings(): Bindings =
    createBindings(DeviceIndex.keyboard, defaultKeyboardGameBindings())
        .plus(createBindings(DeviceIndex.mouse, defaultMouseGameStrokeBindings()))
        .plus(createBindings(DeviceIndex.gamepad, gameGamepadStrokeBindings))



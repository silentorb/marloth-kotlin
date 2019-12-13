package marloth.clienting.input

import silentorb.mythic.haft.*
import org.lwjgl.glfw.GLFW
import silentorb.mythic.haft.*
import simulation.input.CommandType

fun defaultKeyboardStrokeBindings() = mapOf(
    GLFW.GLFW_KEY_ESCAPE to GuiCommandType.menu
)

fun defaultKeyboardMenuBindings() = mapOf(
    GLFW.GLFW_KEY_UP to CommandType.moveUp,
    GLFW.GLFW_KEY_LEFT to CommandType.moveLeft,
    GLFW.GLFW_KEY_RIGHT to CommandType.moveRight,
    GLFW.GLFW_KEY_DOWN to CommandType.moveDown,
    GLFW.GLFW_KEY_ESCAPE to GuiCommandType.menu,
    GLFW.GLFW_KEY_TAB to GuiCommandType.characterInfo,
    GLFW.GLFW_KEY_ENTER to GuiCommandType.menuSelect,
    GLFW.GLFW_KEY_SPACE to GuiCommandType.menuSelect
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

    GAMEPAD_BUTTON_BACK to GuiCommandType.characterInfo,
    GAMEPAD_BUTTON_START to GuiCommandType.menu,
    GAMEPAD_BUTTON_A to GuiCommandType.menuSelect,
    GAMEPAD_BUTTON_B to GuiCommandType.menuBack
)

val firstPersonGamepadBindings = mapOf(
    GAMEPAD_AXIS_RIGHT_UP to CommandType.moveUp,
    GAMEPAD_AXIS_RIGHT_DOWN to CommandType.moveDown,
    GAMEPAD_AXIS_RIGHT_LEFT to CommandType.moveLeft,
    GAMEPAD_AXIS_RIGHT_RIGHT to CommandType.moveRight
)

val allGamepadStrokeBindings = mapOf(
    GAMEPAD_BUTTON_START to GuiCommandType.menu
)

fun allGamepadBindings() =
    commonGamepadBindings()
        .plus(firstPersonGamepadBindings)

fun createDefaultGamepadBindings() =
    createBindings(DeviceIndex.gamepad, allGamepadBindings())
        .plus(createBindings(DeviceIndex.gamepad, allGamepadStrokeBindings))

fun defaultMenuInputProfile() =
    createBindings(DeviceIndex.keyboard, defaultKeyboardMenuBindings())
        .plus(createBindings(DeviceIndex.gamepad, defaultGamepadMenuBindings()))

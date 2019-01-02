package marloth.clienting.input

import haft.*
import org.lwjgl.glfw.GLFW

fun defaultKeyboardStrokeBindings() = mapOf(
    GLFW.GLFW_KEY_ESCAPE to GuiCommandType.menu
)

fun defaultKeyboardMenuBindings() = mapOf(
    GLFW.GLFW_KEY_UP to GuiCommandType.moveUp,
    GLFW.GLFW_KEY_LEFT to GuiCommandType.moveLeft,
    GLFW.GLFW_KEY_RIGHT to GuiCommandType.moveRight,
    GLFW.GLFW_KEY_DOWN to GuiCommandType.moveDown,
    GLFW.GLFW_KEY_ESCAPE to GuiCommandType.menu,
    GLFW.GLFW_KEY_ENTER to GuiCommandType.menuSelect,
    GLFW.GLFW_KEY_SPACE to GuiCommandType.menuSelect
)

fun commonGamepadBindings() = mapOf(
    GAMEPAD_AXIS_LEFT_UP to GuiCommandType.moveUp,
    GAMEPAD_AXIS_LEFT_DOWN to GuiCommandType.moveDown,
    GAMEPAD_AXIS_LEFT_LEFT to GuiCommandType.moveLeft,
    GAMEPAD_AXIS_LEFT_RIGHT to GuiCommandType.moveRight
)

fun defaultGamepadMenuBindings() = mapOf(

    GAMEPAD_AXIS_LEFT_UP to GuiCommandType.moveUp,
    GAMEPAD_AXIS_LEFT_DOWN to GuiCommandType.moveDown,
    GAMEPAD_AXIS_LEFT_LEFT to GuiCommandType.moveLeft,
    GAMEPAD_AXIS_LEFT_RIGHT to GuiCommandType.moveRight,

    GAMEPAD_AXIS_RIGHT_UP to GuiCommandType.moveUp,
    GAMEPAD_AXIS_RIGHT_DOWN to GuiCommandType.moveDown,
    GAMEPAD_AXIS_RIGHT_LEFT to GuiCommandType.moveLeft,
    GAMEPAD_AXIS_RIGHT_RIGHT to GuiCommandType.moveRight,

    GAMEPAD_BUTTON_DPAD_UP to GuiCommandType.moveUp,
    GAMEPAD_BUTTON_DPAD_DOWN to GuiCommandType.moveDown,
    GAMEPAD_BUTTON_DPAD_LEFT to GuiCommandType.moveLeft,
    GAMEPAD_BUTTON_DPAD_RIGHT to GuiCommandType.moveRight,

    GAMEPAD_BUTTON_START to GuiCommandType.menu,
    GAMEPAD_BUTTON_A to GuiCommandType.menuSelect,
    GAMEPAD_BUTTON_B to GuiCommandType.menuBack
)

val firstPersonGamepadBindings = mapOf(
    GAMEPAD_AXIS_RIGHT_UP to GuiCommandType.moveUp,
    GAMEPAD_AXIS_RIGHT_DOWN to GuiCommandType.moveDown,
    GAMEPAD_AXIS_RIGHT_LEFT to GuiCommandType.moveLeft,
    GAMEPAD_AXIS_RIGHT_RIGHT to GuiCommandType.moveRight
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

fun defaultInputProfile(): InputProfile<GuiCommandType> = InputProfile(
    bindings = defaultMenuInputProfile()
)
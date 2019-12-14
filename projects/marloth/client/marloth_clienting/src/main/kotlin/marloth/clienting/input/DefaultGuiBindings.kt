package marloth.clienting.input

import silentorb.mythic.haft.*
import org.lwjgl.glfw.GLFW
import silentorb.mythic.commanding.CommonCharacterCommands

fun defaultKeyboardStrokeBindings() = mapOf(
    GLFW.GLFW_KEY_ESCAPE to GuiCommandType.menu
)

fun defaultKeyboardMenuBindings() = mapOf(
    GLFW.GLFW_KEY_UP to CommonCharacterCommands.moveUp,
    GLFW.GLFW_KEY_LEFT to CommonCharacterCommands.moveLeft,
    GLFW.GLFW_KEY_RIGHT to CommonCharacterCommands.moveRight,
    GLFW.GLFW_KEY_DOWN to CommonCharacterCommands.moveDown,
    GLFW.GLFW_KEY_ESCAPE to GuiCommandType.menu,
    GLFW.GLFW_KEY_TAB to GuiCommandType.characterInfo,
    GLFW.GLFW_KEY_ENTER to GuiCommandType.menuSelect,
    GLFW.GLFW_KEY_SPACE to GuiCommandType.menuSelect
)

fun commonGamepadBindings() = mapOf(
    GAMEPAD_AXIS_LEFT_UP to CommonCharacterCommands.moveUp,
    GAMEPAD_AXIS_LEFT_DOWN to CommonCharacterCommands.moveDown,
    GAMEPAD_AXIS_LEFT_LEFT to CommonCharacterCommands.moveLeft,
    GAMEPAD_AXIS_LEFT_RIGHT to CommonCharacterCommands.moveRight
)

fun defaultGamepadMenuBindings() = mapOf(

    GAMEPAD_AXIS_LEFT_UP to CommonCharacterCommands.moveUp,
    GAMEPAD_AXIS_LEFT_DOWN to CommonCharacterCommands.moveDown,
    GAMEPAD_AXIS_LEFT_LEFT to CommonCharacterCommands.moveLeft,
    GAMEPAD_AXIS_LEFT_RIGHT to CommonCharacterCommands.moveRight,

    GAMEPAD_AXIS_RIGHT_UP to CommonCharacterCommands.moveUp,
    GAMEPAD_AXIS_RIGHT_DOWN to CommonCharacterCommands.moveDown,
    GAMEPAD_AXIS_RIGHT_LEFT to CommonCharacterCommands.moveLeft,
    GAMEPAD_AXIS_RIGHT_RIGHT to CommonCharacterCommands.moveRight,

    GAMEPAD_BUTTON_DPAD_UP to CommonCharacterCommands.moveUp,
    GAMEPAD_BUTTON_DPAD_DOWN to CommonCharacterCommands.moveDown,
    GAMEPAD_BUTTON_DPAD_LEFT to CommonCharacterCommands.moveLeft,
    GAMEPAD_BUTTON_DPAD_RIGHT to CommonCharacterCommands.moveRight,

    GAMEPAD_BUTTON_BACK to GuiCommandType.characterInfo,
    GAMEPAD_BUTTON_START to GuiCommandType.menu,
    GAMEPAD_BUTTON_A to GuiCommandType.menuSelect,
    GAMEPAD_BUTTON_B to GuiCommandType.menuBack
)

val firstPersonGamepadBindings = mapOf(
    GAMEPAD_AXIS_RIGHT_UP to CommonCharacterCommands.moveUp,
    GAMEPAD_AXIS_RIGHT_DOWN to CommonCharacterCommands.moveDown,
    GAMEPAD_AXIS_RIGHT_LEFT to CommonCharacterCommands.moveLeft,
    GAMEPAD_AXIS_RIGHT_RIGHT to CommonCharacterCommands.moveRight
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

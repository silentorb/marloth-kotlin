package marloth.clienting.input

import silentorb.mythic.haft.*
import org.lwjgl.glfw.GLFW
import silentorb.mythic.happenings.CommonCharacterCommands

val gameGamepadStrokeBindings = mapOf(
    GAMEPAD_BUTTON_START to GuiCommandType.menu,

    GAMEPAD_BUTTON_A to CommonCharacterCommands.ability,
    GAMEPAD_BUTTON_DPAD_UP to CommonCharacterCommands.equipSlot0,
    GAMEPAD_BUTTON_DPAD_LEFT to CommonCharacterCommands.equipSlot1,
    GAMEPAD_BUTTON_DPAD_RIGHT to CommonCharacterCommands.equipSlot2,
    GAMEPAD_BUTTON_DPAD_DOWN to CommonCharacterCommands.equipSlot3,

    GAMEPAD_AXIS_LEFT_UP to CommonCharacterCommands.moveUp,
    GAMEPAD_AXIS_LEFT_DOWN to CommonCharacterCommands.moveDown,
    GAMEPAD_AXIS_LEFT_LEFT to CommonCharacterCommands.moveLeft,
    GAMEPAD_AXIS_LEFT_RIGHT to CommonCharacterCommands.moveRight,

    GAMEPAD_AXIS_RIGHT_UP to CommonCharacterCommands.lookUp,
    GAMEPAD_AXIS_RIGHT_DOWN to CommonCharacterCommands.lookDown,
    GAMEPAD_AXIS_RIGHT_LEFT to CommonCharacterCommands.lookLeft,
    GAMEPAD_AXIS_RIGHT_RIGHT to CommonCharacterCommands.lookRight,

    GAMEPAD_BUTTON_A to CommonCharacterCommands.interactPrimary
)

fun defaultMouseGameStrokeBindings() = mapOf(
    GLFW.GLFW_MOUSE_BUTTON_LEFT to CommonCharacterCommands.ability
)

fun defaultKeyboardGameBindings() = mapOf(
    GLFW.GLFW_KEY_W to CommonCharacterCommands.moveUp,
    GLFW.GLFW_KEY_A to CommonCharacterCommands.moveLeft,
    GLFW.GLFW_KEY_D to CommonCharacterCommands.moveRight,
    GLFW.GLFW_KEY_S to CommonCharacterCommands.moveDown,
//    GLFW.GLFW_KEY_TAB to CommandType.switchView,
    GLFW.GLFW_KEY_ENTER to CommonCharacterCommands.interactPrimary
)

fun defaultGameInputBindings(): Bindings =
    createBindings(DeviceIndex.keyboard, defaultKeyboardGameBindings())
        .plus(createBindings(DeviceIndex.mouse, defaultMouseGameStrokeBindings()))
        .plus(createBindings(DeviceIndex.gamepad, gameGamepadStrokeBindings))



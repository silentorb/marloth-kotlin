package marloth.clienting.input

import marloth.clienting.hud.toggleTargetingCommand
import marloth.scenery.enums.CharacterCommands
import marloth.scenery.enums.CharacterRigCommands
import silentorb.mythic.haft.*
import org.lwjgl.glfw.GLFW

val gameGamepadStrokeBindings = mapOf(
    GAMEPAD_BUTTON_START to GuiCommandType.menu,

    GAMEPAD_BUTTON_DPAD_UP to CharacterCommands.equipSlot0,
    GAMEPAD_BUTTON_DPAD_LEFT to CharacterCommands.equipSlot1,
    GAMEPAD_BUTTON_DPAD_RIGHT to CharacterCommands.equipSlot2,
    GAMEPAD_BUTTON_DPAD_DOWN to CharacterCommands.equipSlot3,

    GAMEPAD_AXIS_LEFT_UP to CharacterCommands.moveUp,
    GAMEPAD_AXIS_LEFT_DOWN to CharacterCommands.moveDown,
    GAMEPAD_AXIS_LEFT_LEFT to CharacterCommands.moveLeft,
    GAMEPAD_AXIS_LEFT_RIGHT to CharacterCommands.moveRight,

    GAMEPAD_AXIS_RIGHT_UP to CharacterCommands.lookUp,
    GAMEPAD_AXIS_RIGHT_DOWN to CharacterCommands.lookDown,
    GAMEPAD_AXIS_RIGHT_LEFT to CharacterCommands.lookLeft,
    GAMEPAD_AXIS_RIGHT_RIGHT to CharacterCommands.lookRight,

    GAMEPAD_BUTTON_X to CharacterCommands.ability,
    GAMEPAD_BUTTON_A to CharacterCommands.interactPrimary,
    GAMEPAD_BUTTON_RIGHT_THUMB to toggleTargetingCommand,
    GAMEPAD_BUTTON_BACK to CharacterRigCommands.switchView
)

fun defaultMouseGameStrokeBindings() = mapOf(
    GLFW.GLFW_MOUSE_BUTTON_LEFT to CharacterCommands.ability
)

fun defaultKeyboardGameBindings() = mapOf(
    GLFW.GLFW_KEY_W to CharacterCommands.moveUp,
    GLFW.GLFW_KEY_A to CharacterCommands.moveLeft,
    GLFW.GLFW_KEY_D to CharacterCommands.moveRight,
    GLFW.GLFW_KEY_S to CharacterCommands.moveDown,
    GLFW.GLFW_KEY_TAB to CharacterRigCommands.switchView,
    GLFW.GLFW_KEY_ENTER to CharacterCommands.interactPrimary
)

fun defaultGameInputBindings(): Bindings =
    createBindings(DeviceIndex.keyboard, defaultKeyboardGameBindings())
        .plus(createBindings(DeviceIndex.mouse, defaultMouseGameStrokeBindings()))
        .plus(createBindings(DeviceIndex.gamepad, gameGamepadStrokeBindings))

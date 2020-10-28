package marloth.clienting.input

import marloth.clienting.gui.hud.toggleTargetingCommand
import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.haft.*
import org.lwjgl.glfw.GLFW

val defaultSharedGamepadBindings = mapOf(
    GAMEPAD_BUTTON_BACK to GuiCommandType.characterInfo
)

val gameGamepadBindings = mapOf(
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

    GAMEPAD_BUTTON_X to CharacterCommands.abilityAttack,
    GAMEPAD_BUTTON_Y to CharacterCommands.abilityUtility,
    GAMEPAD_BUTTON_B to CharacterCommands.abilityDefense,
    GAMEPAD_BUTTON_A to CharacterCommands.abilityMobility,
    GAMEPAD_BUTTON_RIGHT_THUMB to toggleTargetingCommand
) + defaultSharedGamepadBindings

fun defaultMouseGameBindings() = mapOf(
    GLFW.GLFW_MOUSE_BUTTON_LEFT to CharacterCommands.abilityAttack
)

val defaultSharedKeyboardBindings = mapOf(
    GLFW.GLFW_KEY_GRAVE_ACCENT to GuiCommandType.editor,
    GLFW.GLFW_KEY_C to GuiCommandType.characterInfo
)

fun defaultKeyboardGameBindings() = mapOf(
    GLFW.GLFW_KEY_W to CharacterCommands.moveUp,
    GLFW.GLFW_KEY_A to CharacterCommands.moveLeft,
    GLFW.GLFW_KEY_D to CharacterCommands.moveRight,
    GLFW.GLFW_KEY_S to CharacterCommands.moveDown,
    GLFW.GLFW_KEY_ENTER to CharacterCommands.interactPrimary,
    GLFW.GLFW_KEY_ESCAPE to GuiCommandType.menu,
    GLFW.GLFW_KEY_E to CharacterCommands.abilityUtility,
    GLFW.GLFW_KEY_Q to CharacterCommands.abilityDefense,
    GLFW.GLFW_KEY_SPACE to CharacterCommands.abilityMobility
) + defaultSharedKeyboardBindings

fun defaultGameInputBindings(): Bindings =
    createBindings(DeviceIndexes.keyboard, defaultKeyboardGameBindings())
        .plus(createBindings(DeviceIndexes.mouse, defaultMouseGameBindings()))
        .plus(createBindings(DeviceIndexes.gamepad, gameGamepadBindings))

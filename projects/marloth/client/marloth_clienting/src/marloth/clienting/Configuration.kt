package marloth.clienting

import commanding.CommandType
import haft.*
import org.lwjgl.glfw.GLFW.*

data class InputConfiguration(
    val profiles: InputProfiles<CommandType>
)

data class Configuration(
    val input: InputConfiguration
)

fun flattenInputProfileBindings(profiles: InputProfiles<CommandType>) =
    profiles.flatMap { it.bindings }

fun createDefaultKeyboardProfile(target: Int) = InputProfile(target,
    createBindings(0, target, mapOf(
        GLFW_KEY_W to CommandType.moveUp,
        GLFW_KEY_A to CommandType.moveLeft,
        GLFW_KEY_D to CommandType.moveRight,
        GLFW_KEY_S to CommandType.moveDown,

        GLFW_KEY_TAB to CommandType.switchView,

        GLFW_KEY_ESCAPE to CommandType.menuBack
    )))

fun createDefaultGamepadProfile(device: Int, target: Int) = InputProfile(target,
    createBindings(device, target, mapOf(
        GAMEPAD_AXIS_LEFT_UP to CommandType.moveUp,
        GAMEPAD_AXIS_LEFT_DOWN to CommandType.moveDown,
        GAMEPAD_AXIS_LEFT_LEFT to CommandType.moveLeft,
        GAMEPAD_AXIS_LEFT_RIGHT to CommandType.moveRight,

        GAMEPAD_BUTTON_BACK to CommandType.switchView,
//        GAMEPAD_BUTTON_BACK to CommandType.menuBack

        GAMEPAD_BUTTON_START to CommandType.menuBack
    )))

fun createNewConfiguration(gamepads: List<Gamepad>): Configuration = Configuration(
    InputConfiguration(listOf(
        createDefaultKeyboardProfile(0)
    ).plus(gamepads.mapIndexed { index, gamepad ->
      createDefaultGamepadProfile(gamepad.id + 1, index)
    }))
)
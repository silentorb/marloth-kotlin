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

//fun flattenInputProfileBindings(profiles: InputProfiles<CommandType>) =
//    profiles.flatMap { it }

fun defaultKeyboardProfile() = mapOf(
    GLFW_KEY_W to CommandType.moveUp,
    GLFW_KEY_A to CommandType.moveLeft,
    GLFW_KEY_D to CommandType.moveRight,
    GLFW_KEY_S to CommandType.moveDown,

    GLFW_KEY_TAB to CommandType.switchView,

    GLFW_KEY_ESCAPE to CommandType.menuBack
)

fun waitingGamepadBinding() = mapOf(
    GAMEPAD_BUTTON_START to CommandType.activateDevice
)

fun defaultGamepadBindings() = mapOf(
    GAMEPAD_AXIS_LEFT_UP to CommandType.moveUp,
    GAMEPAD_AXIS_LEFT_DOWN to CommandType.moveDown,
    GAMEPAD_AXIS_LEFT_LEFT to CommandType.moveLeft,
    GAMEPAD_AXIS_LEFT_RIGHT to CommandType.moveRight,

    GAMEPAD_BUTTON_BACK to CommandType.switchView,
    GAMEPAD_BUTTON_START to CommandType.menuBack
)

data class PlayerInputProfile(
    val player: Int,
    val gameBindings: Bindings<CommandType>,
    val menuBindings: Bindings<CommandType>
)

data class AvailableInputProfiles(
    val playerInputProfiles: List<PlayerInputProfile>
)

fun createPrimaryInputProfile(player: Int, gamepad: Int) =
    PlayerInputProfile(player,
        createBindings(0, player, defaultKeyboardProfile())
            .plus(createBindings(gamepad, player, defaultGamepadBindings())),
        listOf()
    )

fun createSecondaryInputProfile(player: Int, device: Int) =
    PlayerInputProfile(player,
        createBindings(device, player, defaultGamepadBindings()),
        listOf()
    )

fun createDefaultInputProfiles() = listOf(
    createPrimaryInputProfile(1, 2),
    createSecondaryInputProfile(2, 3),
    createSecondaryInputProfile(3, 4),
    createSecondaryInputProfile(4, 5)
)

fun filterAvailableDeviceBindings(bindings: Bindings<CommandType>, devices: List<Int>) =
    bindings.filter { devices.contains(it.device) }

fun selectActiveInputProfiles(activePlayers: List<Int>, playerInputProfiles: List<PlayerInputProfile>) =
    playerInputProfiles.filter { activePlayers.contains(it.player) }
        .map { it.gameBindings }

//fun createNewConfiguration(gamepads: List<Gamepad>): Configuration = Configuration(
//    InputConfiguration(listOf(
//        createBindings(0, 1, defaultKeyboardProfile())
//    ).plus(gamepads.mapIndexed { index, gamepad ->
//      createBindings(gamepad.id + 1, index, defaultGamepadBindings())
//    }))
//)
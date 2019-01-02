package marloth.clienting

import haft.*
import marloth.clienting.input.GuiCommandType

data class InputConfiguration(
    val profiles: InputProfiles<GuiCommandType>
)

data class Configuration(
    val input: InputConfiguration
)

//fun flattenInputProfileBindings(profiles: InputProfiles<GuiCommandType>) =
//    profiles.flatMap { it }

//fun createNewConfiguration(gamepads: List<Gamepad>): Configuration = Configuration(
//    InputConfiguration(listOf(
//        createBindings(0, 1, defaultKeyboardGameBindings())
//    ).plus(gamepads.mapIndexed { index, gamepad ->
//      createBindings(gamepad.id + 1, index, allGamepadBindings())
//    }))
//)
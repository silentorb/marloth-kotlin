package marloth.clienting

import haft.*

data class InputConfiguration(
    val profiles: InputProfiles<CommandType>
)

data class Configuration(
    val input: InputConfiguration
)

//fun flattenInputProfileBindings(profiles: InputProfiles<CommandType>) =
//    profiles.flatMap { it }

//fun createNewConfiguration(gamepads: List<Gamepad>): Configuration = Configuration(
//    InputConfiguration(listOf(
//        createBindings(0, 1, defaultKeyboardGameBindings())
//    ).plus(gamepads.mapIndexed { index, gamepad ->
//      createBindings(gamepad.id + 1, index, allGamepadBindings())
//    }))
//)
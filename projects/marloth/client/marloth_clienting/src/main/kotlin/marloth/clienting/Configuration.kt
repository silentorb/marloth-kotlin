package marloth.clienting

import haft.*

data class InputConfiguration(
    val profiles: InputProfiles
)

data class Configuration(
    val input: InputConfiguration
)

//fun flattenInputProfileBindings(profiles: InputProfiles) =
//    profiles.flatMap { it }

//fun createNewConfiguration(gamepads: List<Gamepad>): Configuration = Configuration(
//    InputConfiguration(listOf(
//        createBindings(0, 1, defaultKeyboardGameBindings())
//    ).plusBounded(gamepads.mapIndexed { index, gamepad ->
//      createBindings(gamepad.id + 1, index, allGamepadBindings())
//    }))
//)

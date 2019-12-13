package marloth.clienting

import silentorb.mythic.haft.*
import silentorb.mythic.haft.InputProfiles

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

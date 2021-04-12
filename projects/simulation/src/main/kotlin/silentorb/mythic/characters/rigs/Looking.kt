package silentorb.mythic.characters.rigs

import silentorb.mythic.cameraman.MomentumAxis
import silentorb.mythic.cameraman.MomentumConfig
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.spatial.Vector2

const val mouseLookEvent = "mouseLookEvent"

fun spiritGamepadMomentumAxis() =
    MomentumAxis(
        horizontal = MomentumConfig(
            positiveIncrement = 0.6f,
            maxVelocity = 6f,
            negativeIncrement = 1.0f
        ),
        vertical = MomentumConfig(
            positiveIncrement = 0.2f,
            maxVelocity = 3f,
            negativeIncrement = 0.3f
        )
    )

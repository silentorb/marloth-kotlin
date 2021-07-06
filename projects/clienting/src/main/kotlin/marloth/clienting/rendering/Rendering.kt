package marloth.clienting.rendering

import silentorb.mythic.debugging.getDebugFloat
import silentorb.mythic.scenery.LightingConfig

fun defaultLightingConfig() =
    LightingConfig(
        ambient = getDebugFloat("AMBIENT_LIGHT_LEVEL") ?: 0f
    )

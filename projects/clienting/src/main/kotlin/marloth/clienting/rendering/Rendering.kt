package marloth.clienting.rendering

import silentorb.mythic.debugging.getDebugFloat
import silentorb.mythic.glowing.globalState
import silentorb.mythic.lookinglass.*
import silentorb.mythic.scenery.Light
import silentorb.mythic.scenery.LightingConfig
import silentorb.mythic.spatial.Vector4i

fun defaultLightingConfig() =
    LightingConfig(
        ambient = getDebugFloat("AMBIENT_LIGHT_LEVEL") ?: 0.08f
    )

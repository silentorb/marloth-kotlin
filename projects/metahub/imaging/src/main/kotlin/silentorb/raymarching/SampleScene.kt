package silentorb.raymarching

import mythic.spatial.Vector3

fun sampleScene(): Sdf =
    plus(
        sphereSdf(Vector3(-0.5f, 0f, 0f), 1f),
        sphereSdf(Vector3(0.7f, 0.3f, 0.3f), 0.7f),
        sphereSdf(Vector3(0.0f, -0.3f, -0.1f), 0.6f)
    )
package simulation.entities

import silentorb.mythic.spatial.Vector4

data class Light(
    val color: Vector4, // w is brightness
    val range: Float
)

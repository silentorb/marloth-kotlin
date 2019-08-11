package rendering

import mythic.spatial.Vector4

data class Material(
    val color: Vector4? = null,
    val glow: Float = 0f,
    val texture: String? = null
)

package physics

import scenery.Shape

data class CollisionObject(
    val shape: Shape,
    val isSolid: Boolean = true
)

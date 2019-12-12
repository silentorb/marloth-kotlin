package simulation.physics

import silentorb.mythic.scenery.Shape

data class CollisionObject(
    val shape: Shape,
    val isSolid: Boolean = true
)

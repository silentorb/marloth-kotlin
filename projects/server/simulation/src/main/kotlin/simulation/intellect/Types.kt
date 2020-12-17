package simulation.intellect

import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector3

typealias Path = List<Id>

data class Pursuit(
    val targetEnemy: Id? = null,
    val targetPosition: Vector3? = null
)

data class Spirit(
    val zone: String? = null,
    val post: Vector3? = null,
    val pursuit: Pursuit? = null
)

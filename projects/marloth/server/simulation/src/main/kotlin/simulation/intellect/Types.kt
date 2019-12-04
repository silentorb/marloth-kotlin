package simulation.intellect

import simulation.intellect.assessment.Knowledge
import mythic.ent.Id
import mythic.spatial.Vector3

typealias Path = List<Id>

data class Pursuit(
    val targetEnemy: Id? = null,
    val targetPosition: Vector3? = null
)

data class Spirit(
    val knowledge: Knowledge? = null,
    val pursuit: Pursuit
)

package simulation.intellect

import simulation.intellect.acessment.Knowledge
import mythic.ent.Entity
import mythic.ent.Id
import mythic.spatial.Vector3

typealias Path = List<Id>

data class Pursuit(
    val targetEnemy: Id? = null,
    val path: Path? = null,
    val targetPosition: Vector3? = null
)

data class Spirit(
    override val id: Id,
    val knowledge: Knowledge? = null,
    val pursuit: Pursuit
) : Entity

package intellect

import intellect.acessment.SpiritKnowledge
import mythic.ent.Entity
import mythic.ent.Id

typealias Path = List<Id>

data class Pursuit(
    val targetEnemy: Id? = null,
    val path: Path? = null
)

data class Spirit(
    override val id: Id,
    val knowledge: SpiritKnowledge? = null,
    val pursuit: Pursuit
) : Entity

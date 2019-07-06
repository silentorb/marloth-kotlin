package simulation.misc

import mythic.ent.Entity
import mythic.ent.Id

data class Faction(
   override val id: Id,
    val name: String
) : Entity

const val misfitsFaction = 1L
const val monstersFaction = 2L

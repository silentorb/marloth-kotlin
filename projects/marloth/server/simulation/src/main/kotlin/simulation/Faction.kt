package simulation

import mythic.ent.Entity
import mythic.ent.Id

data class Faction(
   override val id: Id,
    val name: String
) : Entity
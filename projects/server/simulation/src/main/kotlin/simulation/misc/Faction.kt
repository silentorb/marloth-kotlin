package simulation.misc

import silentorb.mythic.ent.WithId
import silentorb.mythic.ent.Id

data class Faction(
   override val id: Id,
    val name: String
) : WithId

const val misfitFaction = 1L
const val monsterFaction = 2L
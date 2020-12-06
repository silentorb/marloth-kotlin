package simulation.misc

import silentorb.mythic.ent.WithId
import silentorb.mythic.ent.Id

data class Faction(
   override val id: Id,
    val name: String
) : WithId

object Factions {
  val misfits = 1L
  val monsters = 2L
  val neutral = 0L
}

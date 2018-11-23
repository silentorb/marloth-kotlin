package intellect

import mythic.ent.Entity
import mythic.ent.Id
import simulation.*

enum class GoalType {
  kill,
  //  faceTarget,
  beAt
}

data class Goal(
    val id: Id,
    val type: GoalType
)

typealias Goals = List<Goal>

typealias Path = List<Id>

data class Pursuit(
    val target: Id? = null,
    val path: Path? = null
)

data class Spirit(
    override val id: Id,
    val knowledge: SpiritKnowledge? = null,
//    val goals: List<Goal>,
    val pursuit: Pursuit
) : Entity

data class NewSpirit(
    val id: Id
)

fun getNewSpirits(newSpirits: List<NewSpirit>): List<Spirit> =
    newSpirits.map { source ->
      Spirit(
          id = source.id,
          knowledge = null,
          pursuit = Pursuit()
      )
    }
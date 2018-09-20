package intellect

import physics.Body
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

typealias Path = List<Node>

data class Pursuit(
    val target: Id? = null,
    val path: Path? = null
)

data class Spirit(
    override val id: Id,
    val knowledge: Knowledge? = null,
//    val goals: List<Goal>,
    val pursuit: Pursuit
) : EntityLike

data class NewSpirit(
    val id: Id
)
//class Spirit(
//    val child: Character,
//    var state: Spirit
//) {
//  val body: Body
//    get() = child.body
//}


//class AssociatedSpirit(
//    val child: Character,
//    var state: Spirit
//) {
//  val body: Body
//    get() = child.body
//}

fun getNewSpirits(newSpirits: List<NewSpirit>): List<Spirit> =
    newSpirits.map { source ->
      Spirit(
          id = source.id,
          knowledge = null,
          pursuit = Pursuit()
      )
    }
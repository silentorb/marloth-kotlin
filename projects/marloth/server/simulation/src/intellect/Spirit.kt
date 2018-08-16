package intellect

import physics.Body
import simulation.Character
import simulation.Id
import simulation.Node

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
    val id: Id,
    val knowledge: Knowledge,
    val goals: List<Goal>,
    val pursuit: Pursuit
)

//class Spirit(
//    val character: Character,
//    var state: Spirit
//) {
//  val body: Body
//    get() = character.body
//}


class AssociatedSpirit(
    val character: Character,
    var state: Spirit
) {
  val body: Body
    get() = character.body
}
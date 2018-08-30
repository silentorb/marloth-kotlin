package intellect

import physics.Body
import simulation.Character
import simulation.EntityLike
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
    override val id: Id,
    val knowledge: Knowledge,
//    val goals: List<Goal>,
    val pursuit: Pursuit
) : EntityLike

data class NewSpirit(
    val id: Id
)
//class Spirit(
//    val character: Character,
//    var state: Spirit
//) {
//  val body: Body
//    get() = character.body
//}


//class AssociatedSpirit(
//    val character: Character,
//    var state: Spirit
//) {
//  val body: Body
//    get() = character.body
//}

fun getNewSpirits(newSpirits: List<NewSpirit>, newCharacters: List<Character>): List<Spirit> =
    newSpirits.map { source ->
      Spirit(
          id = source.id,
          knowledge = Knowledge(
              character = newCharacters.first { it.id == source.id },
              nodes = listOf(),
              visibleCharacters = listOf()
          ),
          pursuit = Pursuit()
      )
    }
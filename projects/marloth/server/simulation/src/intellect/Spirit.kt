package intellect

import physics.Body
import simulation.Character
import simulation.Id
import simulation.Node

enum class GoalType {
  kill,
  faceTarget,
  beAt
}

data class Goal(
    val type: GoalType,
    val dependencies: List<Goal> = listOf(),
    val target: Id? = null
)

typealias Goals = List<Goal>

data class SpiritState(
    val goals: List<Goal>,
    val knowledge: Knowledge
)

class Spirit(
    val character: Character,
    var state: SpiritState
) {
  val body: Body
    get() = character.body
}
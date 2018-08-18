package simulation

import mythic.spatial.Vector3
import physics.MovementForce

enum class ActionType {
  attack,
  face,
  move
}

data class Action(
    val type: ActionType,
    val force: MovementForce? = null,
    val facingRotation: Vector3? = null

)

typealias Actions = List<Action>

data class CharacterAction(
    val character: Character,
    val actions: Actions
)

typealias CharacterActions = List<CharacterAction>
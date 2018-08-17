package simulation.changing

import physics.Forces
import simulation.ActionType
import simulation.CharacterActions
import simulation.World

fun actionsToForces(actions: CharacterActions): Forces =
    actions.flatMap { ca -> ca.actions.mapNotNull { it.force } }

fun applyActions(world: World, actions: CharacterActions) {
  for (a in actions) {
    val character = a.character
    for (action in a.actions.filter { it.force == null }) {
      when (action.type) {
        ActionType.face -> character.facingRotation = action.facingRotation!!
        else -> throw Error("Not supported")
      }
    }
  }
}
package simulation.changing

import physics.Forces
import simulation.CharacterActions
import simulation.World

fun actionsToForces(world: World, actions: CharacterActions): Forces =
    actions.flatMap { ca -> ca.actions.mapNotNull { it.force } }

fun applyActions(world: World, actions: CharacterActions) {

}
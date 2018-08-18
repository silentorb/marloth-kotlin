package simulation.changing

import physics.Forces
import simulation.Action
import simulation.ActionType
import simulation.Character
import simulation.CharacterActions
import simulation.World

//fun actionsToForces(actions: CharacterActions): Forces =
//    actions.flatMap { ca -> ca.actions.mapNotNull { it.force } }
//
////fun applyAction(world: World, character: Character, action: Action) {
////  when (action.type) {
//////    ActionType.attack ->
////    ActionType.face -> character.facingRotation = action.facingRotation!!
////    else -> throw Error("Not supported")
////  }
////}
//
//fun applyActions(world: World, actions: CharacterActions) {
//  for (a in actions) {
//    val character = a.character
//    for (action in a.actions.filter { it.force == null }) {
//      applyAction(world, character, action)
//    }
//  }
//}
//
//fun processNewMissiles()
package simulation.intellect.execution

import mythic.ent.Id
import mythic.spatial.Vector3
import simulation.entities.Action
import simulation.input.Command
import simulation.input.CommandType
import simulation.input.Commands
import simulation.intellect.Pursuit
import simulation.intellect.assessment.Knowledge
import simulation.main.Deck
import simulation.main.World
import simulation.physics.Body
import simulation.physics.SimpleBody

fun shouldMoveDirectlyToward(deck: Deck, target: SimpleBody, attacker: Id): Boolean {
  val attackerBody = deck.bodies[attacker]!!
  throw Error("Not implemented")
//  return !isInAttackRange(attackerBody, target.position, deck.characters[attacker]!!.abilities[0])
//      && attackerBody.nearestNode == target.nearestNode
}

fun spiritAttack(world: World, character: Id, knowledge: Knowledge, pursuit: Pursuit): Commands {
  val attacker = character
  val target = knowledge.characters[pursuit.targetEnemy]!!
  val body = world.deck.bodies[attacker]!!
  val offset = target.position - body.position
  return spiritNeedsFacing(world, character, offset, 0.05f) {
    listOf(Command(CommandType.ability, attacker))
  }
}

// This is to ensure that a spirit attack when a target is cleanly within range
// instead of along the knife's-edge-fringe of its weapon range.
const val spiritAttackRangeBuffer = 0.1f

//fun isInAttackRange(attackerBody: Body, target: Vector3, ability: Action): Boolean =
//    attackerBody.position.distance(target) <= ability.definition.range - spiritAttackRangeBuffer


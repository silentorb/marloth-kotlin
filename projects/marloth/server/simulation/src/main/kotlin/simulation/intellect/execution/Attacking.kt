package simulation.intellect.execution

import simulation.intellect.Pursuit
import simulation.intellect.acessment.Knowledge
import mythic.ent.Id
import mythic.spatial.Vector3
import simulation.physics.Body
import simulation.physics.SimpleBody
import simulation.main.Deck
import simulation.main.World
import simulation.entities.Ability
import simulation.input.Command
import simulation.input.CommandType
import simulation.input.Commands

fun shouldMoveDirectlyToward(deck: Deck, target: SimpleBody, attacker: Id): Boolean {
  val attackerBody = deck.bodies[attacker]!!
  return !isInAttackRange(attackerBody, target.position, deck.characters[attacker]!!.abilities[0])
      && attackerBody.nearestNode == target.nearestNode
}

fun spiritAttack(world: World, knowledge: Knowledge, pursuit: Pursuit): Commands {
  val attacker = knowledge.spiritId
  val target = knowledge.characters[pursuit.targetEnemy]!!
  val body = world.deck.bodies[attacker]!!
  val offset = target.position - body.position
  return spiritNeedsFacing(world, knowledge, offset, 0.05f) {
    listOf(Command(CommandType.attack, attacker))
  }
}

// This is to ensure that a spirit attack when a target is cleanly within range
// instead of along the knife's-edge-fringe of its weapon range.
const val spiritAttackRangeBuffer = 0.1f

fun isInAttackRange(attackerBody: Body, target: Vector3, ability: Ability): Boolean =
    attackerBody.position.distance(target) <= ability.definition.range - spiritAttackRangeBuffer


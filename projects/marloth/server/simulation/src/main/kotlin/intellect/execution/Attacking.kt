package intellect.execution

import intellect.Pursuit
import intellect.acessment.Knowledge
import mythic.ent.Id
import mythic.spatial.Vector3
import physics.Body
import physics.SimpleBody
import simulation.*

fun shouldMoveDirectlyToward(deck: Deck, target: SimpleBody, attacker: Id): Boolean {
  val attackerBody = deck.bodies[attacker]!!
  return !isInAttackRange(attackerBody, target.position, deck.characters[attacker]!!.abilities[0])
      && attackerBody.node == target.node
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


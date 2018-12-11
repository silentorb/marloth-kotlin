package intellect.execution

import intellect.Pursuit
import intellect.acessment.Knowledge
import mythic.spatial.Vector3
import physics.Body
import simulation.*

fun spiritAttack(world: World, knowledge: Knowledge, pursuit: Pursuit): Commands {
  val target = knowledge.characters[pursuit.targetEnemy]!!

  val character = world.characterTable[knowledge.spiritId]!!
  val body = world.bodyTable[knowledge.spiritId]!!
  val offset = target.position - body.position
  return if (target.lastSeen > 0.5f || !isInAttackRange(body, target.position, character.abilities[0])) {
    // It is assumed that if we are here then the pursuit already brought the attacker to the same node as the target
    // and now the attacker just needs to get closer in a direct line
    moveSpirit(world, knowledge, target.position)
  } else
    spiritNeedsFacing(world, knowledge, offset, 0.05f) {
      listOf(Command(CommandType.attack, character.id))
    }
}

// This is to ensure that a spirit attack when a target is cleanly within range
// instead of along the knife's-edge-fringe of its weapon range.
private const val spiritAttackRangeBuffer = 0.1f

fun isInAttackRange(attackerBody: Body, target: Vector3, ability: Ability): Boolean =
    attackerBody.position.distance(target) <= ability.definition.range - spiritAttackRangeBuffer


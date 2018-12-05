package intellect.execution

import intellect.Pursuit
import intellect.acessment.Knowledge
import intellect.acessment.canSee
import physics.Body
import simulation.*

fun spiritAttack(knowledge: Knowledge, pursuit: Pursuit): Commands {
  val world = knowledge.world
  val target = knowledge.visibleCharacters
      .map { knowledge.world.characterTable[it]!! }
      .first { it.id == pursuit.targetEnemy }
  val character = world.characterTable[knowledge.spiritId]!!
  val body = world.bodyTable[knowledge.spiritId]!!
  val targetBody = world.bodyTable[target.id]!!
  val offset = targetBody.position - body.position
  return if (!isInAttackRange(body, targetBody, character.abilities[0])) {
    // It is assumed that if we are here then the pursuit already brought the attacker to the same node as the target
    // and now the attacker just needs to get closer in a direct line
    moveSpirit(knowledge, targetBody.position)
  } else
    spiritNeedsFacing(knowledge, offset, 0.1f) {
      listOf(Command(CommandType.attack, character.id))
    }
}

// This is to ensure that a spirit attack when a target is cleanly within range
// instead of along the knife's-edge-fringe of its weapon range.
private const val spiritAttackRangeBuffer = 0.1f

fun isInAttackRange(attackerBody: Body, targetBody: Body, ability: Ability): Boolean =
    attackerBody.position.distance(targetBody.position) <= ability.definition.range - spiritAttackRangeBuffer


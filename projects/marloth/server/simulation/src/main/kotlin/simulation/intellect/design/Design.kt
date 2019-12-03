package simulation.intellect.design

import mythic.ent.Id
import simulation.entities.getActiveAction
import simulation.intellect.Pursuit
import simulation.intellect.assessment.Knowledge
import simulation.intellect.assessment.getVisibleEnemies
import simulation.intellect.execution.spiritAttackRangeBuffer
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Definitions

fun updateTargetEnemy(world: World, character: Id, knowledge: Knowledge, pursuit: Pursuit): Id? {
  val visibleEnemies = getVisibleEnemies(world.deck.characters[character]!!, knowledge)
  return if (pursuit.targetEnemy != null && visibleEnemies.any { it.id == pursuit.targetEnemy })
    pursuit.targetEnemy
  else if (visibleEnemies.any())
    visibleEnemies.first().id
  else
    null
}

fun getActionRange(deck: Deck, definitions: Definitions, action: Id): Float {
  val accessory = deck.accessories[action]!!
  val definition = definitions.actions[accessory.type]!!
  return definition.range
}

fun updatePursuit(world: World, character: Id, knowledge: Knowledge, pursuit: Pursuit): Pursuit {
  val targetEnemy = updateTargetEnemy(world, character, knowledge, pursuit)
  val target = knowledge.characters[pursuit.targetEnemy]
  val (path, targetPosition) = if (target != null) {
    val deck = world.deck
    val bodies = deck.bodies
    val attackerBody = bodies[character]!!
    val action = getActiveAction(deck, character)!!
    val range = getActionRange(deck, world.definitions, action) - spiritAttackRangeBuffer
    val distance = attackerBody.position.distance(target.position)
    val gap = distance - range
    if (gap > 0f && attackerBody.nearestNode == target.nearestNode)
      Pair(null, (target.position - attackerBody.position).normalize() * gap)
    else
      Pair(updateAttackMovementPath(world, character, knowledge, target.id, pursuit.path), null)
  } else if (false) {
    // Guard room
    Pair(null, null)
  } else
//    Pair(updateRoamingPath(world, knowledge, pursuit), null)
    Pair(null, updateRoamingTargetPosition(world, character, knowledge, pursuit))

  return Pursuit(
      targetEnemy = targetEnemy,
      path = path,
      targetPosition = targetPosition
  )
}
